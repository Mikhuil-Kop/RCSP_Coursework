//Заполнить файл данными с сервера
$(document).ready(function () {
    $.ajax({
        headers: {
            Accept: 'application/json'
        },
        type: 'GET',
        url: "/api/?func=get-character-detailed",
        data: {
            'mode': 'get',
        },
        complete: function (responce) {
            let data = JSON.parse(responce);
            console.log(data);
        }
    });
}