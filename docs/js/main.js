/*$(window).scroll(function (event) {
    var scroll = $(window).scrollTop();
    // Do something
    console.log("SCROLL TOP");
    var height = $(window).scrollTop();

    if(height  > 1) {
        // do something
        console.log("Scroll Height !!");

    }
});


$(document).ready(function(){
    $("div").scroll(function(){

    });
  });
*/

document.addEventListener('DOMContentLoaded', function() {
    // your code here
    console.log("Page Loaded !")
 }, false);

window.onscroll = function (ev) {
    var B = document.body; //IE 'quirks'
    var D = document.documentElement; //IE with doctype
    D = (D.clientHeight) ? D : B;

    if (D.scrollTop == 0) {
        console.log("SCROLL TOP");
        //  document.getElementById("navbar1").setAttribute("class", "navbar navbar-light bg-light fixed-top navbar-bg");
        $(".navbar-fixed-top").addClass("navbar-bg");
        $('.navbar-fixed-top').removeClass("navbar-bg-trans");

        document.getElementById("appnamenav").setAttribute("class", "header-text app-name-style");


        

    } else {
        $('.navbar-fixed-top').removeClass("navbar-bg");
        $(".navbar-fixed-top").addClass("navbar-bg-trans");

        document.getElementById("appnamenav").setAttribute("class", "header-text");

    }


};
/*
$(document).ready(function(){
    $("div").scroll(function(){
        if(scroll_start > offset.top) {
            $(".navbar-fixed-top").addClass("navbar-bg");
        } else {
            $('.navbar-fixed-top').removeClass("navbar-bg");
        }
    });
  });*/


if (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
    // is mobile..
    console.log("Mobile User");
   document.getElementById("header1").setAttribute("style", " text-align: center; padding-top: 10px;");

   //document.getElementById("halfss1").style.display="none";

   //document.getElementById("subheader1").setAttribute("class", "");
   //document.getElementById("subheader1").setAttribute("style", "text-align:center: padding-top:50px;");

  //document.getElementById("subheader1").setAttribute("class", " box-header-mobile ");
  document.getElementById("col1mobile").setAttribute("style", " color: #fff; ");
  document.getElementById("col1desktop").setAttribute("style", "display:none;");


}else{
    //For Desktop

    document.getElementById("col1mobile").setAttribute("style", "display:none;");
    document.getElementById("col1desktop").setAttribute("style", "color: #fff; text-align: right;");

}


$('.owl-carousel').owlCarousel({
    loop:true,
    margin:10,
    nav:true,
    responsiveClass:true,
    stagePadding: 50,

    responsive:{
        0:{
            items:1
        },
        600:{
            items:3
        },
        1000:{
            items:5
        }
    }
})