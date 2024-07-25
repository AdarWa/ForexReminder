const picker = flatpickr("#timedate", {
    enableTime: true,
    dateFormat: "d-m-Y H:i",
    time_24hr: true
});

function uuid() {
    return "10000000-1000-4000-8000-100000000000".replace(/[018]/g, c =>
      (+c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> +c / 4).toString(16)
    );
  }
  

document.querySelector('form').addEventListener('submit', function(event) { 
    event.preventDefault();
    if(picker._input.value == ""){
        alert("Please fill the date.");
        return;
    }
        
    let command = {type: "WRITE", reminder: {uuid: uuid(), enabled: document.getElementById("enabled").checked, date: picker._input.value,sound:$("#sound").val(), entries: []}};
    for(comp of template.components){
        const el = document.getElementById(comp.name.replace(" ", "_"));
        if(comp.type == "STRING"){
            command.reminder.entries.push({key: comp, value: el.value});
        }else {
            command.reminder.entries.push({key: comp, value: el.checked});
        }
        
    }
    $.post({
        url: "/",
        data:JSON.stringify(command),
        dataType: "json",
        contentType: "json",
        success: function (result) {
            if(result.code != 0){
                alert("Code " + result.code + ". Message: " + result.message);
                console.log("Code " + result.code + ". Message: " + result.message);
                return;
            }
            close();
        }
    });
    console.log(JSON.stringify(command));
});    

$("#sound").parent().on("click", function(){
    $.post({
        url: "/",
        timeout: 0,
        data:JSON.stringify({type:"CHOOSEFILE"}),
        dataType: "json",
        contentType: "json",
        success: function (result) {
            if(result.code != 2 && result.code != 0){
                alert("Code " + result.code + ". Message: " + result.message);
                console.log("Code " + result.code + ". Message: " + result.message);
                return;
            }
            if(result.code == 2){
                $("#sound").val(result.message);
            }else {
                $("#sound").val("");
            }
            
        }
    });
});