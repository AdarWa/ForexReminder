package net.adarw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.j256.simplemagic.ContentType;
import fi.iki.elonen.NanoHTTPD;
import net.adarw.Utils.StorageUtils;
import net.adarw.alertListner.Listener;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;


public class Server extends NanoHTTPD {

    Logger logger = Logger.getLogger(Server.class.getName());
    MainInterface main;

    public String readResource(final String fileName, Charset charset) {
        try {
            return Resources.toString(Resources.getResource(fileName), charset);
        } catch (IOException e) {
            String err = "Error while trying to read resource: " + e.getMessage();
            logger.severe(err);
            return err;
        }
    }

    public Server(MainInterface main){
        super(Settings.current.serverBind,Settings.current.port);
        this.main = main;
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException e) {
            logger.severe("Error while starting http server, " + e.getMessage());
            logger.severe("Address already in use. Shutting down.");
            System.exit(-1);
        }
    }


    @Override
    public Response serve(IHTTPSession session) {
        if(!Settings.current.allowCrossOriginRequests) {
            String origin = session.getHeaders().get("origin");
            if (origin != null && !origin.equals(String.format("http://%s:%d", Settings.current.serverBind, Settings.current.port))) {
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Forbidden");
            }
        }
        Map<String, String> params = new HashMap<>();
        Method method = session.getMethod();
        if (Method.POST.equals(method)) {
            try {
                session.parseBody(params);
            } catch (IOException ioe) {
                logger.severe("SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            } catch (ResponseException re) {
                logger.severe("ERROR: " + re.getMessage());
                return newFixedLengthResponse(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
            }

            String postBody = params.get("postData");
            return newFixedLengthResponse(main.OnCommand(postBody));
        }else if(Method.GET.equals(method)){
            URI url;
            try {
                url = new URI(session.getUri());
            } catch (URISyntaxException e) {
                logger.severe("Bad URI: " + e.getMessage());
                return newFixedLengthResponse("Bad URI: " + e.getMessage());
            }
            String path = url.getPath();
            if(path.equals("/")){
                Listener.working = true;
                Main.getInstance().listener.interrupt();
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                    logger.severe("Sleep Interrupted");
                }
                return newFixedLengthResponse(readResource("gui/index.html", StandardCharsets.UTF_8)
                        .replace("{Body}", TemplateParser.getReminderTable())
                        .replace("{Template}", TemplateParser.getTemplateTable()));
            }else if(path.equals("/add")){
                return newFixedLengthResponse(readResource("gui/add.html", StandardCharsets.UTF_8)
                        .replace("{Template}", new Gson().toJson(StorageUtils.getTemplate(), Template.class))
                        .replace("{Controls}", TemplateParser.getControlTemplate())
                        .replace("{sound}", Settings.current.alertDefaultSoundFile));
            }else if(path.equals("/settings.js")){
                return newFixedLengthResponse(Response.Status.OK,ContentType.fromFileExtension("js").getMimeType() ,readResource("gui/settings.js", StandardCharsets.UTF_8)
                        .replace("{minutesPerHeartbeat}", String.valueOf(Settings.current.minutesPerHeartbeat))
                        .replace("{showDialogOnDelete}", String.valueOf(Settings.current.showDialogOnDelete)));
            }else if(path.equals("/main.js")){
                return newFixedLengthResponse(Response.Status.OK,ContentType.fromFileExtension("js").getMimeType() ,readResource("gui/main.js", StandardCharsets.UTF_8)
                        .replace("{{interval}}", String.valueOf(Settings.current.pageReloadMinutesInterval*60*1000)));
            }else if(path.equals("/settings")){
                try {
                    return newFixedLengthResponse(readResource("gui/settings.html", StandardCharsets.UTF_8)
                            .replace("{settingsTemplate}", Settings.current.serializeToJson()));
                } catch (IllegalAccessException | JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }else if(path.equals("/template-editor")){
                return newFixedLengthResponse(readResource("gui/template-editor.html", StandardCharsets.UTF_8)
                        .replace("{templateData}", new Gson().toJson(StorageUtils.getTemplate())));
            }else{
                String[] split = path.split("\\.");
                return newFixedLengthResponse(Response.Status.OK,ContentType.fromFileExtension(split[split.length-1]).getMimeType() ,readResource("gui"+path, StandardCharsets.UTF_8));
            }
        }
        return newFixedLengthResponse("Error!");
    }

}
