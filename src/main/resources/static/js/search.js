$('#modal-content').on('shown.bs.modal', function () {
    var term = $( "#searchTerm" ).val();
    $.post( "/search", { query: term })
      .done(function( data ) {
        $( "#searchTerm" ).val("");
        $(".modal-body").text("");
        $(".resultsTerm").text(term);
        term = "";
        data.forEach(function(d) {
            var jsonn = JSON.parse(d.sentiment);
            d.sentiment = jsonn;
            if(d.sentiment && d.sentiment.score){
                d.sentiment.score = d.sentiment.score.substring(0,6);
            } else {
                d.sentiment = {
                    score: 1
                };
                d.sentiment.score = "0.00";
            }
            if(d.sentiment.score < 0 ) {
                d.arrow = "bottom";
                d.color = "red";
            } else {
                d.arrow = "top";
                d.color = "green";
            }
          });
        $("#clientTemplate").tmpl(data).appendTo( ".modal-body" );
//        data.forEach(function(d) {
//            $(".modal-body").append(d.title);
//            $(".modal-body").append("<br/>");
//        });
      });
});

$('#openBtn').click(function () {
    $('#modal-content').modal({
        show: true
    });
});

formSubmit = function() {
    $('#modal-content').modal({
        show: true
    });
}

$('#modal-content').on('hidden.bs.modal', function () {
    $(".modal-body").text("");
})