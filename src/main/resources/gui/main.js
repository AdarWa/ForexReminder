let selected = [];

function addReminder(){
    let params = `scrollbars=no,resizable=no,status=no,location=no,toolbar=no,menubar=no,
    width=450,height=720,left=750,top=180`;

    const win = open('/add', 'Add Reminder', params);

    win.onbeforeunload = function(){
        location.reload();
    }
}

function isOpen(){
    return $(".menu").css("visibility") == "visible";
}

function openMenu(){
    if(isOpen()){
        $(".menu").css("visibility","hidden");
    }else{
        $(".menu").css("visibility","visible");
    }    
}

document.addEventListener('click', function() {
    if(isOpen()){
        openMenu();
    }
}, true);


function clearAll(){
    $.post({
        url: "/",
        data:JSON.stringify({type:"CLEAR"}),
        dataType: "json",
        contentType: "json",
        success: function (result) {
            if(result.code != 0){
                alert("Code " + result.code + ". Message: " + result.message);
                console.log("Code " + result.code + ". Message: " + result.message);
                return;
            }
            location.reload();
        }
    });
}

function trashDialog(title, func){
    if(!showDialogOnDelete){
        func();
    }
    $("#dialog").text(title);
    $("#dialog").dialog({

        autoOpen: true,
        buttons: [
    
            {
                text: "Yes",
                click: function() {
                    func();
                    $("#dialog").text("");
                    $(this).dialog("close");
            }
        },
        {
            text: "No",
            click: function() {
                $("#dialog").text("");
                $(this).dialog("close");
            },
        }
        ],
        width: 400
    
    });
}

$("#clear").on("click", function(){
    trashDialog("Are you sure you want to delete all reminders?", clearAll);
});

$(".trash").on("click", function(){
    if(selected.length == 0) return;
    let s = "";
    if(selected.length > 1){
        s = "s";
    }
    trashDialog("Are you sure you want to delete the selected reminder" + s + "?", function(){
        for(item of selected){
            const uuid = $(item).attr("uuid");
            $.post({
                url: "/",
                data:JSON.stringify({type:"DELETE", uuid: uuid}),
                dataType: "json",
                contentType: "json",
                async: false,
                success: function (result) {
                    if(result.code != 0){
                        alert("Code " + result.code + ". Message: " + result.message);
                        console.log("Code " + result.code + ". Message: " + result.message);
                        return;
                    }
                }
            });
        }
        location.reload();
    });
});

$(".reminder").on("click", function(){
    if(selected.includes(this)){
        $(this).attr("style", "");        
        selected = selected.filter(item => item !== this);
        return;
    }
    $(this).attr("style", "background-color:antiquewhite !important");
    selected.push(this);
});

$(".reminder").dblclick(function(){
    const uuid = $(this).attr("uuid");
    let params = `scrollbars=no,resizable=no,status=no,location=no,toolbar=no,menubar=no,
    width=450,height=720,left=750,top=180`;

    const win = open('/add?edit='+uuid, 'Edit Reminder', params);

    win.onbeforeunload = function(){
        location.reload();
    }
});


function heartbeat(){
    $.post({
        url: "/",
        data:JSON.stringify({type:"HEARTBEAT"}),
        dataType: "json",
        contentType: "json",
        success: function (result) {
            if(result.code != 0){
                alert("Code " + result.code + ". Message: " + result.message);
                console.log("Code " + result.code + ". Message: " + result.message);
                return;
            }
            $(".error").css("visibility", "hidden");
        },
        error: function(){
            $(".error").css("visibility", "visible");
        }

    });
}
setInterval(heartbeat,minutesPerHeartbeat*60000);

$("#openSet").on("click", function(){
    $.post({
        url: "/",
        data:JSON.stringify({type:"SETTINGS", operation:"open"}),
        dataType: "json",
        contentType: "json",
        success: function (result) {
            if(result.code != 0){
                alert("Code " + result.code + ". Message: " + result.message);
                console.log("Code " + result.code + ". Message: " + result.message);
                return;
            }
        }
    });
});

$("#openSetGui").on("click", function(){
    let params = `scrollbars=no,resizable=no,status=no,location=no,toolbar=no,menubar=no,
        width=720,height=720,left=750,top=180`;

    open('/settings', 'Settings', params);
});



$("#reloadSet").on("click", function(){
    $.post({
        url: "/",
        data:JSON.stringify({type:"SETTINGS", operation:"reload"}),
        dataType: "json",
        contentType: "json",
        success: function (result) {
            if(result.code != 0){
                alert("Code " + result.code + ". Message: " + result.message);
                console.log("Code " + result.code + ". Message: " + result.message);
                return;
            }
            location.reload();
        }
    });
});

$("#quit").on("click", function(){
    $.post({
        url: "/",
        data:JSON.stringify({type:"SETTINGS", operation:"quit"}),
        dataType: "json",
        contentType: "json",
        success: function () {
            heartbeat();
            close();
        },
        error: function(){
            heartbeat();
            close();
        }
    });
});

$("#default").on("click", function(){

    $("#dialog").text("This operation will delete all reminders, templates, settings and logs.\nAre you sure you want to proceed?\nYou will need to start the core again after you lose connection.");
    $("#dialog").dialog({

        autoOpen: true,
        buttons: [
    
            {
                text: "Yes",
                click: function() {
                    $.post({
                        url: "/",
                        data:JSON.stringify({type:"SETTINGS", operation:"default"}),
                        dataType: "json",
                        contentType: "json",
                        success: function () {
                            heartbeat();
                        },
                        error: function(){
                            heartbeat();
                        }
                    });
                    $("#dialog").text("");
                    $(this).dialog("close");
            }
        },
        {
            text: "No",
            click: function() {
                $("#dialog").text("");
                $(this).dialog("close");
            },
        }
        ],
        width: 400
    
    });
});

$("#openTem").on("click", function(){
    $("#dialog").text("You will need to restart the core after every change done to the template.\n Please note that if you change the settings, you can click the reload settings button and no restart will be required.");
    $("#dialog").dialog({

        autoOpen: true,
        buttons: [
    
            {
                text: "Ok",
                click: function() {
                    $.post({
                        url: "/",
                        data:JSON.stringify({type:"SETTINGS", operation:"openTemplate"}),
                        dataType: "json",
                        contentType: "json",
                        success: function () {
                            if(result.code != 0){
                                alert("Code " + result.code + ". Message: " + result.message);
                                console.log("Code " + result.code + ". Message: " + result.message);
                                return;
                            }
                        },
                        error: function(){
                            heartbeat();
                        }
                    });
                    $("#dialog").text("");
                    $(this).dialog("close");
            }
        }
        ],
        width: 400
    
    });
});

$("#openDir").on("click", function(){
    $.post({
        url: "/",
        data:JSON.stringify({type:"SETTINGS", operation:"openDir"}),
        dataType: "json",
        contentType: "json",
        success: function () {
            if(result.code != 0){
                alert("Code " + result.code + ". Message: " + result.message);
                console.log("Code " + result.code + ". Message: " + result.message);
                return;
            }
        },
        error: function(){
            heartbeat();
        }
    });
});

$(".import").on("click", function(){
    $.post({
        url: "/",
        data:JSON.stringify({type:"CHOOSEFILE", file:"csv"}),
        dataType: "json",
        contentType: "json",
        success: function (result) {
            if(result.code != 0 && result.code != 2){
                alert("Code " + result.code + ". Message: " + result.message);
                console.log("Code " + result.code + ". Message: " + result.message);
                return;
            }
            if(result.code == 2){
                $.post({
                    url: "/",
                    data:JSON.stringify({type:"IMPORTFX", path:result.message}),
                    dataType: "json",
                    contentType: "json",
                    success: function (r) {
                        if(r.code != 0){
                            alert("Code " + r.code + ". Message: " + r.message);
                            console.log("Code " + r.code + ". Message: " + r.message);
                            return;
                        }
                        location.reload();
                    },
                    error: function(){
                        heartbeat();
                    }
                });
            }
        },
        error: function(){
            heartbeat();
        }
    });
});

setInterval(function(){
    location.reload();
}, {{interval}})