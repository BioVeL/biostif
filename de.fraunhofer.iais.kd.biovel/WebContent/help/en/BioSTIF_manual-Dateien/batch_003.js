if(AJS.DarkFeatures.isEnabled("navlinks.menu")){AJS.toInit(function(A){A.getJSON(AJS.params.contextPath+"/rest/menu/latest/home",function(C){var B=A(Navlinks.Templates.Menus.applicationsMenu({navLinkEntityList:C}));
var D=A("#header-menu-bar");
D.prepend(B);
B.ajsMenuItem({isFixedPosition:true})
})
})
};
// This file was automatically generated from menu.soy.
// Please don't edit this file by hand.

if (typeof Navlinks == 'undefined') { var Navlinks = {}; }
if (typeof Navlinks.Templates == 'undefined') { Navlinks.Templates = {}; }
if (typeof Navlinks.Templates.Menus == 'undefined') { Navlinks.Templates.Menus = {}; }


Navlinks.Templates.Menus.applicationsMenu = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  if (opt_data.navLinkEntityList.length > 0) {
    output.append('<li id="ajs-app-menu" class="normal ajs-menu-item"><a id="applications-menu-link" class="trigger ajs-menu-title" href="#"><span><span>', soy.$$escapeHtml("Anwendungen"), '</span></span></a><div class="ajs-drop-down assistive"><ul id="applications-menu-link-global" class="section-global first">');
    var navLinkEntityList8 = opt_data.navLinkEntityList;
    var navLinkEntityListLen8 = navLinkEntityList8.length;
    for (var navLinkEntityIndex8 = 0; navLinkEntityIndex8 < navLinkEntityListLen8; navLinkEntityIndex8++) {
      var navLinkEntityData8 = navLinkEntityList8[navLinkEntityIndex8];
      Navlinks.Templates.Menus.applicationsMenuItem({label: navLinkEntityData8.label, link: navLinkEntityData8.link}, output);
    }
    output.append('</ul></div></li>');
  }
  return opt_sb ? '' : output.toString();
};


Navlinks.Templates.Menus.applicationsMenuItem = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<li><a href="', soy.$$escapeHtml(opt_data.link), '">', soy.$$escapeHtml(opt_data.label), '</a></li>');
  return opt_sb ? '' : output.toString();
};

