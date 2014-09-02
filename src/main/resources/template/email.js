function template(locals) {
var buf = [];
var jade_mixins = {};
var jade_interp;
;var locals_for_with = (locals || {});(function (emaildata) {
buf.push("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><meta name=\"viewport\" content=\"width=device-width\"/><title>my jade template</title><style type=\"text/css\">/**********************************************\n* Ink v1.0.5 - Copyright 2013 ZURB Inc        *\n**********************************************/\n\n/* Client-specific Styles & Reset */\n\n#outlook a { \n  padding:0; \n} \n\nbody{ \n  width:100% !important; \n  min-width: 100%;\n  -webkit-text-size-adjust:100%; \n  -ms-text-size-adjust:100%; \n  margin:0; \n  padding:0;\n}\n\n.ExternalClass { \n  width:100%;\n} \n\n.ExternalClass, \n.ExternalClass p, \n.ExternalClass span, \n.ExternalClass font, \n.ExternalClass td, \n.ExternalClass div { \n  line-height: 100%; \n} \n\n#backgroundTable { \n  margin:0; \n  padding:0; \n  width:100% !important; \n  line-height: 100% !important; \n}\n\nimg { \n  outline:none; \n  text-decoration:none; \n  -ms-interpolation-mode: bicubic;\n  width: auto;\n  max-width: 100%; \n  float: left; \n  clear: both; \n  display: block;\n}\n\ncenter {\n  width: 100%;\n  min-width: 580px;\n}\n\na img { \n  border: none;\n}\n\np {\n  margin: 0 0 0 10px;\n}\n\ntable {\n  border-spacing: 0;\n  border-collapse: collapse;\n}\n\ntd { \n  word-break: break-word;\n  -webkit-hyphens: auto;\n  -moz-hyphens: auto;\n  hyphens: auto;\n  border-collapse: collapse !important; \n}\n\ntable, tr, td {\n  padding: 0;\n  vertical-align: top;\n  text-align: left;\n}\n\nhr {\n  color: #d9d9d9; \n  background-color: #d9d9d9; \n  height: 1px; \n  border: none;\n}\n\n/* Responsive Grid */\n\ntable.body {\n  height: 100%;\n  width: 100%;\n}\n\ntable.container {\n  width: 580px;\n  margin: 0 auto;\n  text-align: inherit;\n}\n\ntable.row { \n  padding: 0px; \n  width: 100%;\n  position: relative;\n}\n\ntable.container table.row {\n  display: block;\n}\n\ntd.wrapper {\n  padding: 10px 20px 0px 0px;\n  position: relative;\n}\n\ntable.columns,\ntable.column {\n  margin: 0 auto;\n}\n\ntable.columns td,\ntable.column td {\n  padding: 0px 0px 10px; \n}\n\ntable.columns td.sub-columns,\ntable.column td.sub-columns,\ntable.columns td.sub-column,\ntable.column td.sub-column {\n  padding-right: 10px;\n}\n\ntd.sub-column, td.sub-columns {\n  min-width: 0px;\n}\n\ntable.row td.last,\ntable.container td.last {\n  padding-right: 0px;\n}\n\ntable.one { width: 30px; }\ntable.two { width: 80px; }\ntable.three { width: 130px; }\ntable.four { width: 180px; }\ntable.five { width: 230px; }\ntable.six { width: 280px; }\ntable.seven { width: 330px; }\ntable.eight { width: 380px; }\ntable.nine { width: 430px; }\ntable.ten { width: 480px; }\ntable.eleven { width: 530px; }\ntable.twelve { width: 580px; }\n\ntable.one center { min-width: 30px; }\ntable.two center { min-width: 80px; }\ntable.three center { min-width: 130px; }\ntable.four center { min-width: 180px; }\ntable.five center { min-width: 230px; }\ntable.six center { min-width: 280px; }\ntable.seven center { min-width: 330px; }\ntable.eight center { min-width: 380px; }\ntable.nine center { min-width: 430px; }\ntable.ten center { min-width: 480px; }\ntable.eleven center { min-width: 530px; }\ntable.twelve center { min-width: 580px; }\n\ntable.one .panel center { min-width: 10px; }\ntable.two .panel center { min-width: 60px; }\ntable.three .panel center { min-width: 110px; }\ntable.four .panel center { min-width: 160px; }\ntable.five .panel center { min-width: 210px; }\ntable.six .panel center { min-width: 260px; }\ntable.seven .panel center { min-width: 310px; }\ntable.eight .panel center { min-width: 360px; }\ntable.nine .panel center { min-width: 410px; }\ntable.ten .panel center { min-width: 460px; }\ntable.eleven .panel center { min-width: 510px; }\ntable.twelve .panel center { min-width: 560px; }\n\n.body .columns td.one,\n.body .column td.one { width: 8.333333%; }\n.body .columns td.two,\n.body .column td.two { width: 16.666666%; }\n.body .columns td.three,\n.body .column td.three { width: 25%; }\n.body .columns td.four,\n.body .column td.four { width: 33.333333%; }\n.body .columns td.five,\n.body .column td.five { width: 41.666666%; }\n.body .columns td.six,\n.body .column td.six { width: 50%; }\n.body .columns td.seven,\n.body .column td.seven { width: 58.333333%; }\n.body .columns td.eight,\n.body .column td.eight { width: 66.666666%; }\n.body .columns td.nine,\n.body .column td.nine { width: 75%; }\n.body .columns td.ten,\n.body .column td.ten { width: 83.333333%; }\n.body .columns td.eleven,\n.body .column td.eleven { width: 91.666666%; }\n.body .columns td.twelve,\n.body .column td.twelve { width: 100%; }\n\ntd.offset-by-one { padding-left: 50px; }\ntd.offset-by-two { padding-left: 100px; }\ntd.offset-by-three { padding-left: 150px; }\ntd.offset-by-four { padding-left: 200px; }\ntd.offset-by-five { padding-left: 250px; }\ntd.offset-by-six { padding-left: 300px; }\ntd.offset-by-seven { padding-left: 350px; }\ntd.offset-by-eight { padding-left: 400px; }\ntd.offset-by-nine { padding-left: 450px; }\ntd.offset-by-ten { padding-left: 500px; }\ntd.offset-by-eleven { padding-left: 550px; }\n\ntd.expander {\n  visibility: hidden;\n  width: 0px;\n  padding: 0 !important;\n}\n\ntable.columns .text-pad,\ntable.column .text-pad {\n  padding-left: 10px;\n  padding-right: 10px;\n}\n\ntable.columns .left-text-pad,\ntable.columns .text-pad-left,\ntable.column .left-text-pad,\ntable.column .text-pad-left {\n  padding-left: 10px;\n}\n\ntable.columns .right-text-pad,\ntable.columns .text-pad-right,\ntable.column .right-text-pad,\ntable.column .text-pad-right {\n  padding-right: 10px;\n}\n\n/* Block Grid */\n\n.block-grid {\n  width: 100%;\n  max-width: 580px;\n}\n\n.block-grid td {\n  display: inline-block;\n  padding:10px;\n}\n\n.two-up td {\n  width:270px;\n}\n\n.three-up td {\n  width:173px;\n}\n\n.four-up td {\n  width:125px;\n}\n\n.five-up td {\n  width:96px;\n}\n\n.six-up td {\n  width:76px;\n}\n\n.seven-up td {\n  width:62px;\n}\n\n.eight-up td {\n  width:52px;\n}\n\n/* Alignment & Visibility Classes */\n\ntable.center, td.center {\n  text-align: center;\n}\n\nh1.center,\nh2.center,\nh3.center,\nh4.center,\nh5.center,\nh6.center {\n  text-align: center;\n}\n\nspan.center {\n  display: block;\n  width: 100%;\n  text-align: center;\n}\n\nimg.center {\n  margin: 0 auto;\n  float: none;\n}\n\n.show-for-small,\n.hide-for-desktop {\n  display: none;\n}\n\n/* Typography */\n\nbody, table.body, h1, h2, h3, h4, h5, h6, p, td { \n  color: #222222;\n  font-family: \"Helvetica\", \"Arial\", sans-serif; \n  font-weight: normal; \n  padding:0; \n  margin: 0;\n  text-align: left; \n  line-height: 1.3;\n}\n\nh1, h2, h3, h4, h5, h6 {\n  word-break: normal;\n}\n\nh1 {font-size: 40px;}\nh2 {font-size: 36px;}\nh3 {font-size: 32px;}\nh4 {font-size: 28px;}\nh5 {font-size: 24px;}\nh6 {font-size: 20px;}\nbody, table.body, p, td {font-size: 14px;line-height:19px;}\n\np.lead, p.lede, p.leed {\n  font-size: 18px;\n  line-height:21px;\n}\n\np { \n  margin-bottom: 10px;\n}\n\nsmall {\n  font-size: 10px;\n}\n\na {\n  color: #2ba6cb; \n  text-decoration: none;\n}\n\na:hover { \n  color: #2795b6 !important;\n}\n\na:active { \n  color: #2795b6 !important;\n}\n\na:visited { \n  color: #2ba6cb !important;\n}\n\nh1 a, \nh2 a, \nh3 a, \nh4 a, \nh5 a, \nh6 a {\n  color: #2ba6cb;\n}\n\nh1 a:active, \nh2 a:active,  \nh3 a:active, \nh4 a:active, \nh5 a:active, \nh6 a:active { \n  color: #2ba6cb !important; \n} \n\nh1 a:visited, \nh2 a:visited,  \nh3 a:visited, \nh4 a:visited, \nh5 a:visited, \nh6 a:visited { \n  color: #2ba6cb !important; \n} \n\n/* Panels */\n\n.panel {\n  background: #f2f2f2;\n  border: 1px solid #d9d9d9;\n  padding: 10px !important;\n}\n\n.sub-grid table {\n  width: 100%;\n}\n\n.sub-grid td.sub-columns {\n  padding-bottom: 0;\n}\n\n/* Buttons */\n\ntable.button,\ntable.tiny-button,\ntable.small-button,\ntable.medium-button,\ntable.large-button {\n  width: 100%;\n  overflow: hidden;\n}\n\ntable.button td,\ntable.tiny-button td,\ntable.small-button td,\ntable.medium-button td,\ntable.large-button td {\n  display: block;\n  width: auto !important;\n  text-align: center;\n  background: #2ba6cb;\n  border: 1px solid #2284a1;\n  color: #ffffff;\n  padding: 8px 0;\n}\n\ntable.tiny-button td {\n  padding: 5px 0 4px;\n}\n\ntable.small-button td {\n  padding: 8px 0 7px;\n}\n\ntable.medium-button td {\n  padding: 12px 0 10px;\n}\n\ntable.large-button td {\n  padding: 21px 0 18px;\n}\n\ntable.button td a,\ntable.tiny-button td a,\ntable.small-button td a,\ntable.medium-button td a,\ntable.large-button td a {\n  font-weight: bold;\n  text-decoration: none;\n  font-family: Helvetica, Arial, sans-serif;\n  color: #ffffff;\n  font-size: 16px;\n}\n\ntable.tiny-button td a {\n  font-size: 12px;\n  font-weight: normal;\n}\n\ntable.small-button td a {\n  font-size: 16px;\n}\n\ntable.medium-button td a {\n  font-size: 20px;\n}\n\ntable.large-button td a {\n  font-size: 24px;\n}\n\ntable.button:hover td,\ntable.button:visited td,\ntable.button:active td {\n  background: #2795b6 !important;\n}\n\ntable.button:hover td a,\ntable.button:visited td a,\ntable.button:active td a {\n  color: #fff !important;\n}\n\ntable.button:hover td,\ntable.tiny-button:hover td,\ntable.small-button:hover td,\ntable.medium-button:hover td,\ntable.large-button:hover td {\n  background: #2795b6 !important;\n}\n\ntable.button:hover td a,\ntable.button:active td a,\ntable.button td a:visited,\ntable.tiny-button:hover td a,\ntable.tiny-button:active td a,\ntable.tiny-button td a:visited,\ntable.small-button:hover td a,\ntable.small-button:active td a,\ntable.small-button td a:visited,\ntable.medium-button:hover td a,\ntable.medium-button:active td a,\ntable.medium-button td a:visited,\ntable.large-button:hover td a,\ntable.large-button:active td a,\ntable.large-button td a:visited {\n  color: #ffffff !important; \n}\n\ntable.secondary td {\n  background: #e9e9e9;\n  border-color: #d0d0d0;\n  color: #555;\n}\n\ntable.secondary td a {\n  color: #555;\n}\n\ntable.secondary:hover td {\n  background: #d0d0d0 !important;\n  color: #555;\n}\n\ntable.secondary:hover td a,\ntable.secondary td a:visited,\ntable.secondary:active td a {\n  color: #555 !important;\n}\n\ntable.success td {\n  background: #5da423;\n  border-color: #457a1a;\n}\n\ntable.success:hover td {\n  background: #457a1a !important;\n}\n\ntable.alert td {\n  background: #c60f13;\n  border-color: #970b0e;\n}\n\ntable.alert:hover td {\n  background: #970b0e !important;\n}\n\ntable.radius td {\n  -webkit-border-radius: 3px;\n  -moz-border-radius: 3px;\n  border-radius: 3px;\n}\n\ntable.round td {\n  -webkit-border-radius: 500px;\n  -moz-border-radius: 500px;\n  border-radius: 500px;\n}\n\n/* Outlook First */\n\nbody.outlook p {\n  display: inline !important;\n}\n\n/*  Media Queries */\n\n@media only screen and (max-width: 600px) {\n\n  table[class=\"body\"] img {\n    width: auto !important;\n    height: auto !important;\n  }\n\n  table[class=\"body\"] center {\n    min-width: 0 !important;\n  }\n\n  table[class=\"body\"] .container {\n    width: 95% !important;\n  }\n\n  table[class=\"body\"] .row {\n    width: 100% !important;\n    display: block !important;\n  }\n\n  table[class=\"body\"] .wrapper {\n    display: block !important;\n    padding-right: 0 !important;\n  }\n\n  table[class=\"body\"] .columns,\n  table[class=\"body\"] .column {\n    table-layout: fixed !important;\n    float: none !important;\n    width: 100% !important;\n    padding-right: 0px !important;\n    padding-left: 0px !important;\n    display: block !important;\n  }\n\n  table[class=\"body\"] .wrapper.first .columns,\n  table[class=\"body\"] .wrapper.first .column {\n    display: table !important;\n  }\n\n  table[class=\"body\"] table.columns td,\n  table[class=\"body\"] table.column td {\n    width: 100% !important;\n  }\n\n  table[class=\"body\"] .columns td.one,\n  table[class=\"body\"] .column td.one { width: 8.333333% !important; }\n  table[class=\"body\"] .columns td.two,\n  table[class=\"body\"] .column td.two { width: 16.666666% !important; }\n  table[class=\"body\"] .columns td.three,\n  table[class=\"body\"] .column td.three { width: 25% !important; }\n  table[class=\"body\"] .columns td.four,\n  table[class=\"body\"] .column td.four { width: 33.333333% !important; }\n  table[class=\"body\"] .columns td.five,\n  table[class=\"body\"] .column td.five { width: 41.666666% !important; }\n  table[class=\"body\"] .columns td.six,\n  table[class=\"body\"] .column td.six { width: 50% !important; }\n  table[class=\"body\"] .columns td.seven,\n  table[class=\"body\"] .column td.seven { width: 58.333333% !important; }\n  table[class=\"body\"] .columns td.eight,\n  table[class=\"body\"] .column td.eight { width: 66.666666% !important; }\n  table[class=\"body\"] .columns td.nine,\n  table[class=\"body\"] .column td.nine { width: 75% !important; }\n  table[class=\"body\"] .columns td.ten,\n  table[class=\"body\"] .column td.ten { width: 83.333333% !important; }\n  table[class=\"body\"] .columns td.eleven,\n  table[class=\"body\"] .column td.eleven { width: 91.666666% !important; }\n  table[class=\"body\"] .columns td.twelve,\n  table[class=\"body\"] .column td.twelve { width: 100% !important; }\n\n  table[class=\"body\"] td.offset-by-one,\n  table[class=\"body\"] td.offset-by-two,\n  table[class=\"body\"] td.offset-by-three,\n  table[class=\"body\"] td.offset-by-four,\n  table[class=\"body\"] td.offset-by-five,\n  table[class=\"body\"] td.offset-by-six,\n  table[class=\"body\"] td.offset-by-seven,\n  table[class=\"body\"] td.offset-by-eight,\n  table[class=\"body\"] td.offset-by-nine,\n  table[class=\"body\"] td.offset-by-ten,\n  table[class=\"body\"] td.offset-by-eleven {\n    padding-left: 0 !important;\n  }\n\n  table[class=\"body\"] table.columns td.expander {\n    width: 1px !important;\n  }\n\n  table[class=\"body\"] .right-text-pad,\n  table[class=\"body\"] .text-pad-right {\n    padding-left: 10px !important;\n  }\n\n  table[class=\"body\"] .left-text-pad,\n  table[class=\"body\"] .text-pad-left {\n    padding-right: 10px !important;\n  }\n\n  table[class=\"body\"] .hide-for-small,\n  table[class=\"body\"] .show-for-desktop {\n    display: none !important;\n  }\n\n  table[class=\"body\"] .show-for-small,\n  table[class=\"body\"] .hide-for-desktop {\n    display: inherit !important;\n  }\n}\n</style></head><body><table class=\"body\"><tr><td align=\"center\" valign=\"top\" class=\"center\"><center><table class=\"row header\"><tr><td align=\"center\" valign=\"top\" class=\"center\"><center><table class=\"container\"><tr><td class=\"wrapper last\"><table class=\"twelve columns\"><tr><td><h1>Recruiter Activity Report</h1></td><td class=\"expander\"></td></tr></table></td></tr></table></center></td></tr></table><br/><table class=\"container\"><tr><td><!-- content start-->");
// iterate emaildata
;(function(){
  var $$obj = emaildata;
  if ('number' == typeof $$obj.length) {

    for (var i = 0, $$l = $$obj.length; i < $$l; i++) {
      var recruiter = $$obj[i];

buf.push("<table class=\"row\"><tr><td class=\"wrapper\"><table class=\"four columns\"><tr><!-- Name should come here--><td><h6>" + (jade.escape(null == (jade_interp = recruiter.name) ? "" : jade_interp)) + "</h6></td><td class=\"expander\"></td></tr></table></td><td class=\"wrapper last\"><table class=\"eight columns\"><tr><td class=\"panel\">");
if ( recruiter.jobactivity)
{
buf.push("<h5>Job Activities</h5><hr width=\"100%\"/>");
// iterate recruiter.jobactivity
;(function(){
  var $$obj = recruiter.jobactivity;
  if ('number' == typeof $$obj.length) {

    for (var ji = 0, $$l = $$obj.length; ji < $$l; ji++) {
      var jobactivity = $$obj[ji];

buf.push("<div><a" + (jade.attr("href", "" + (ji) + "", true, false)) + ">" + (jade.escape((jade_interp = jobactivity.title) == null ? '' : jade_interp)) + "</a><br/>" + (jade.escape((jade_interp = jobactivity.status) == null ? '' : jade_interp)) + "");
if ( jobactivity.isOpen)
{
buf.push(" (Open)");
}
if ( jobactivity.history)
{
buf.push("<div style=\"margin-bottom:5px;\">");
// iterate jobactivity.history
;(function(){
  var $$obj = jobactivity.history;
  if ('number' == typeof $$obj.length) {

    for (var hi = 0, $$l = $$obj.length; hi < $$l; hi++) {
      var history = $$obj[hi];

buf.push("<div>" + (jade.escape((jade_interp = history.columnName) == null ? '' : jade_interp)) + " changed from \"" + (jade.escape((jade_interp = history.oldValue) == null ? '' : jade_interp)) + "\" to \"" + (jade.escape((jade_interp = history.newValue) == null ? '' : jade_interp)) + "\"</div>");
    }

  } else {
    var $$l = 0;
    for (var hi in $$obj) {
      $$l++;      var history = $$obj[hi];

buf.push("<div>" + (jade.escape((jade_interp = history.columnName) == null ? '' : jade_interp)) + " changed from \"" + (jade.escape((jade_interp = history.oldValue) == null ? '' : jade_interp)) + "\" to \"" + (jade.escape((jade_interp = history.newValue) == null ? '' : jade_interp)) + "\"</div>");
    }

  }
}).call(this);

buf.push("</div>");
}
if ( jobactivity.activities)
{
buf.push("<div style=\"margin-bottom:5px;\">");
// iterate jobactivity.activities
;(function(){
  var $$obj = jobactivity.activities;
  if ('number' == typeof $$obj.length) {

    for (var ai = 0, $$l = $$obj.length; ai < $$l; ai++) {
      var jactivity = $$obj[ai];

buf.push("<div>" + (jade.escape((jade_interp = ai) == null ? '' : jade_interp)) + " - " + (jade.escape((jade_interp = jactivity) == null ? '' : jade_interp)) + "</div>");
    }

  } else {
    var $$l = 0;
    for (var ai in $$obj) {
      $$l++;      var jactivity = $$obj[ai];

buf.push("<div>" + (jade.escape((jade_interp = ai) == null ? '' : jade_interp)) + " - " + (jade.escape((jade_interp = jactivity) == null ? '' : jade_interp)) + "</div>");
    }

  }
}).call(this);

buf.push("</div>");
}
buf.push("</div><br/>");
    }

  } else {
    var $$l = 0;
    for (var ji in $$obj) {
      $$l++;      var jobactivity = $$obj[ji];

buf.push("<div><a" + (jade.attr("href", "" + (ji) + "", true, false)) + ">" + (jade.escape((jade_interp = jobactivity.title) == null ? '' : jade_interp)) + "</a><br/>" + (jade.escape((jade_interp = jobactivity.status) == null ? '' : jade_interp)) + "");
if ( jobactivity.isOpen)
{
buf.push(" (Open)");
}
if ( jobactivity.history)
{
buf.push("<div style=\"margin-bottom:5px;\">");
// iterate jobactivity.history
;(function(){
  var $$obj = jobactivity.history;
  if ('number' == typeof $$obj.length) {

    for (var hi = 0, $$l = $$obj.length; hi < $$l; hi++) {
      var history = $$obj[hi];

buf.push("<div>" + (jade.escape((jade_interp = history.columnName) == null ? '' : jade_interp)) + " changed from \"" + (jade.escape((jade_interp = history.oldValue) == null ? '' : jade_interp)) + "\" to \"" + (jade.escape((jade_interp = history.newValue) == null ? '' : jade_interp)) + "\"</div>");
    }

  } else {
    var $$l = 0;
    for (var hi in $$obj) {
      $$l++;      var history = $$obj[hi];

buf.push("<div>" + (jade.escape((jade_interp = history.columnName) == null ? '' : jade_interp)) + " changed from \"" + (jade.escape((jade_interp = history.oldValue) == null ? '' : jade_interp)) + "\" to \"" + (jade.escape((jade_interp = history.newValue) == null ? '' : jade_interp)) + "\"</div>");
    }

  }
}).call(this);

buf.push("</div>");
}
if ( jobactivity.activities)
{
buf.push("<div style=\"margin-bottom:5px;\">");
// iterate jobactivity.activities
;(function(){
  var $$obj = jobactivity.activities;
  if ('number' == typeof $$obj.length) {

    for (var ai = 0, $$l = $$obj.length; ai < $$l; ai++) {
      var jactivity = $$obj[ai];

buf.push("<div>" + (jade.escape((jade_interp = ai) == null ? '' : jade_interp)) + " - " + (jade.escape((jade_interp = jactivity) == null ? '' : jade_interp)) + "</div>");
    }

  } else {
    var $$l = 0;
    for (var ai in $$obj) {
      $$l++;      var jactivity = $$obj[ai];

buf.push("<div>" + (jade.escape((jade_interp = ai) == null ? '' : jade_interp)) + " - " + (jade.escape((jade_interp = jactivity) == null ? '' : jade_interp)) + "</div>");
    }

  }
}).call(this);

buf.push("</div>");
}
buf.push("</div><br/>");
    }

  }
}).call(this);

}
if ( recruiter.allactivity)
{
buf.push("<h5>All Activites</h5><hr width=\"100%\"/>");
// iterate recruiter.allactivity
;(function(){
  var $$obj = recruiter.allactivity;
  if ('number' == typeof $$obj.length) {

    for (var ai = 0, $$l = $$obj.length; ai < $$l; ai++) {
      var activity = $$obj[ai];

buf.push("<div>" + (jade.escape((jade_interp = ai) == null ? '' : jade_interp)) + " - " + (jade.escape((jade_interp = activity) == null ? '' : jade_interp)) + "</div>");
    }

  } else {
    var $$l = 0;
    for (var ai in $$obj) {
      $$l++;      var activity = $$obj[ai];

buf.push("<div>" + (jade.escape((jade_interp = ai) == null ? '' : jade_interp)) + " - " + (jade.escape((jade_interp = activity) == null ? '' : jade_interp)) + "</div>");
    }

  }
}).call(this);

}
buf.push("</td><td class=\"expander\"></td></tr></table></td></tr></table>");
    }

  } else {
    var $$l = 0;
    for (var i in $$obj) {
      $$l++;      var recruiter = $$obj[i];

buf.push("<table class=\"row\"><tr><td class=\"wrapper\"><table class=\"four columns\"><tr><!-- Name should come here--><td><h6>" + (jade.escape(null == (jade_interp = recruiter.name) ? "" : jade_interp)) + "</h6></td><td class=\"expander\"></td></tr></table></td><td class=\"wrapper last\"><table class=\"eight columns\"><tr><td class=\"panel\">");
if ( recruiter.jobactivity)
{
buf.push("<h5>Job Activities</h5><hr width=\"100%\"/>");
// iterate recruiter.jobactivity
;(function(){
  var $$obj = recruiter.jobactivity;
  if ('number' == typeof $$obj.length) {

    for (var ji = 0, $$l = $$obj.length; ji < $$l; ji++) {
      var jobactivity = $$obj[ji];

buf.push("<div><a" + (jade.attr("href", "" + (ji) + "", true, false)) + ">" + (jade.escape((jade_interp = jobactivity.title) == null ? '' : jade_interp)) + "</a><br/>" + (jade.escape((jade_interp = jobactivity.status) == null ? '' : jade_interp)) + "");
if ( jobactivity.isOpen)
{
buf.push(" (Open)");
}
if ( jobactivity.history)
{
buf.push("<div style=\"margin-bottom:5px;\">");
// iterate jobactivity.history
;(function(){
  var $$obj = jobactivity.history;
  if ('number' == typeof $$obj.length) {

    for (var hi = 0, $$l = $$obj.length; hi < $$l; hi++) {
      var history = $$obj[hi];

buf.push("<div>" + (jade.escape((jade_interp = history.columnName) == null ? '' : jade_interp)) + " changed from \"" + (jade.escape((jade_interp = history.oldValue) == null ? '' : jade_interp)) + "\" to \"" + (jade.escape((jade_interp = history.newValue) == null ? '' : jade_interp)) + "\"</div>");
    }

  } else {
    var $$l = 0;
    for (var hi in $$obj) {
      $$l++;      var history = $$obj[hi];

buf.push("<div>" + (jade.escape((jade_interp = history.columnName) == null ? '' : jade_interp)) + " changed from \"" + (jade.escape((jade_interp = history.oldValue) == null ? '' : jade_interp)) + "\" to \"" + (jade.escape((jade_interp = history.newValue) == null ? '' : jade_interp)) + "\"</div>");
    }

  }
}).call(this);

buf.push("</div>");
}
if ( jobactivity.activities)
{
buf.push("<div style=\"margin-bottom:5px;\">");
// iterate jobactivity.activities
;(function(){
  var $$obj = jobactivity.activities;
  if ('number' == typeof $$obj.length) {

    for (var ai = 0, $$l = $$obj.length; ai < $$l; ai++) {
      var jactivity = $$obj[ai];

buf.push("<div>" + (jade.escape((jade_interp = ai) == null ? '' : jade_interp)) + " - " + (jade.escape((jade_interp = jactivity) == null ? '' : jade_interp)) + "</div>");
    }

  } else {
    var $$l = 0;
    for (var ai in $$obj) {
      $$l++;      var jactivity = $$obj[ai];

buf.push("<div>" + (jade.escape((jade_interp = ai) == null ? '' : jade_interp)) + " - " + (jade.escape((jade_interp = jactivity) == null ? '' : jade_interp)) + "</div>");
    }

  }
}).call(this);

buf.push("</div>");
}
buf.push("</div><br/>");
    }

  } else {
    var $$l = 0;
    for (var ji in $$obj) {
      $$l++;      var jobactivity = $$obj[ji];

buf.push("<div><a" + (jade.attr("href", "" + (ji) + "", true, false)) + ">" + (jade.escape((jade_interp = jobactivity.title) == null ? '' : jade_interp)) + "</a><br/>" + (jade.escape((jade_interp = jobactivity.status) == null ? '' : jade_interp)) + "");
if ( jobactivity.isOpen)
{
buf.push(" (Open)");
}
if ( jobactivity.history)
{
buf.push("<div style=\"margin-bottom:5px;\">");
// iterate jobactivity.history
;(function(){
  var $$obj = jobactivity.history;
  if ('number' == typeof $$obj.length) {

    for (var hi = 0, $$l = $$obj.length; hi < $$l; hi++) {
      var history = $$obj[hi];

buf.push("<div>" + (jade.escape((jade_interp = history.columnName) == null ? '' : jade_interp)) + " changed from \"" + (jade.escape((jade_interp = history.oldValue) == null ? '' : jade_interp)) + "\" to \"" + (jade.escape((jade_interp = history.newValue) == null ? '' : jade_interp)) + "\"</div>");
    }

  } else {
    var $$l = 0;
    for (var hi in $$obj) {
      $$l++;      var history = $$obj[hi];

buf.push("<div>" + (jade.escape((jade_interp = history.columnName) == null ? '' : jade_interp)) + " changed from \"" + (jade.escape((jade_interp = history.oldValue) == null ? '' : jade_interp)) + "\" to \"" + (jade.escape((jade_interp = history.newValue) == null ? '' : jade_interp)) + "\"</div>");
    }

  }
}).call(this);

buf.push("</div>");
}
if ( jobactivity.activities)
{
buf.push("<div style=\"margin-bottom:5px;\">");
// iterate jobactivity.activities
;(function(){
  var $$obj = jobactivity.activities;
  if ('number' == typeof $$obj.length) {

    for (var ai = 0, $$l = $$obj.length; ai < $$l; ai++) {
      var jactivity = $$obj[ai];

buf.push("<div>" + (jade.escape((jade_interp = ai) == null ? '' : jade_interp)) + " - " + (jade.escape((jade_interp = jactivity) == null ? '' : jade_interp)) + "</div>");
    }

  } else {
    var $$l = 0;
    for (var ai in $$obj) {
      $$l++;      var jactivity = $$obj[ai];

buf.push("<div>" + (jade.escape((jade_interp = ai) == null ? '' : jade_interp)) + " - " + (jade.escape((jade_interp = jactivity) == null ? '' : jade_interp)) + "</div>");
    }

  }
}).call(this);

buf.push("</div>");
}
buf.push("</div><br/>");
    }

  }
}).call(this);

}
if ( recruiter.allactivity)
{
buf.push("<h5>All Activites</h5><hr width=\"100%\"/>");
// iterate recruiter.allactivity
;(function(){
  var $$obj = recruiter.allactivity;
  if ('number' == typeof $$obj.length) {

    for (var ai = 0, $$l = $$obj.length; ai < $$l; ai++) {
      var activity = $$obj[ai];

buf.push("<div>" + (jade.escape((jade_interp = ai) == null ? '' : jade_interp)) + " - " + (jade.escape((jade_interp = activity) == null ? '' : jade_interp)) + "</div>");
    }

  } else {
    var $$l = 0;
    for (var ai in $$obj) {
      $$l++;      var activity = $$obj[ai];

buf.push("<div>" + (jade.escape((jade_interp = ai) == null ? '' : jade_interp)) + " - " + (jade.escape((jade_interp = activity) == null ? '' : jade_interp)) + "</div>");
    }

  }
}).call(this);

}
buf.push("</td><td class=\"expander\"></td></tr></table></td></tr></table>");
    }

  }
}).call(this);

buf.push("</td></tr></table></center></td></tr></table></body></html>");}.call(this,"emaildata" in locals_for_with?locals_for_with.emaildata:typeof emaildata!=="undefined"?emaildata:undefined));;return buf.join("");
}