var MW=MW||{};
var $=$||AJS.$;
MW.MV=MW.MV||{};
AJS.toInit(function(A){if(AJS.Meta&&!AJS.Meta.get("remote-user")){return 
}MW.MV.AnchorManager=function(){var H=contextPath,E=H+"/plugins/servlet/notifications-miniview?parentUrl="+encodeURIComponent(window.location.href),K=[0,"0"],L={JIRA:"JIRA",CONFLUENCE:"Confluence"};
function J(){if(typeof (Confluence)!="undefined"){return L.CONFLUENCE
}else{if(typeof (JIRA)!="undefined"){return L.JIRA
}}}function N(){if(!AJS.DarkFeatures.isEnabled("confluence.new-header")){M()
}A("#notifications-anchor").html('<div class="badge-i"><span class="badge-w"><span class="badge"></span></span></div>')
}function M(){var P=J(),O;
if(P==L.JIRA){O=A("#header .global .secondary")
}else{if(P==L.CONFLUENCE){O=A("#header-menu-bar")
}}O.children().each(function(){A(this).find("a").click(function(){MW.Dialog.hide()
})
});
O.append("<a href='#' id='notifications-anchor' class='mw-anchor read'></a>")
}function G(O,Q){A("#notifications-anchor .badge").html(Q);
var R=O>0?AJS.format("Neue Benachrichtigungen: {0}com.atlassian.mywork.back.to.tasks=Zur\u00FCck zu Aufgaben",O):"Offene Benachrichtigungen";
var P=A("#notifications-anchor").attr("title")||"";
A("#notifications-anchor").attr("title",R+P.replace(/.*?\(/," ("));
A("#notifications-anchor").toggleClass("unread",O!==0);
A("#notifications-anchor").toggleClass("read",O===0);
K=[O,Q]
}function I(){console.log("Creating iframes");
var O="gn";
if(L.CONFLUENCE==J()){O=O.split("")
}B("notifications",E,O);
C()
}function C(){A(document).keydown(function(O){if(AJS.InlineDialog.current&&O.which==27&&!A(O.target).is(":input")){AJS.InlineDialog.current.hide()
}})
}function F(){A("#header-menu-bar").find(".ajs-drop-down").each(function(){this.hide()
})
}function B(V,T,Q){var U;
var R=function(){U=this
};
var P=function(){A("#notifications-anchor").focus()
};
if(!window.addEventListener){window.attachEvent("onmessage",S)
}else{window.addEventListener("message",S,false)
}function S(W){if("escKey"===W.data){U.hide()
}}MW.Dialog=AJS.InlineDialog(A("#"+V+"-anchor"),V+"-miniview",function(Y,W,Z){if(A(Y).children().length===0){A(Y).append(A('<iframe id="'+V+'-miniview-iframe" src="'+T+"&unread="+K[1]+'" frameborder="0"></iframe>'))
}var X=JSON.stringify({unread:K[1]});
A("#"+V+"-miniview-iframe")[0].contentWindow.postMessage(X,"*");
A("#"+V+"-anchor").one("click",function(){if(A(Y).is(":visible")){U.hide()
}});
F();
Z()
},{width:500,height:520,hideDelay:null,initCallback:R,hideCallback:P});
var O=function(){AJS.whenIType(Q).click(("#"+V+"-anchor"));
G(K[0],K[1])
};
if(window.Confluence){AJS.bind("initialize.keyboardshortcuts",O)
}else{O()
}if(/[?&]show-miniview/.test(window.location.search)){A("#"+V+"-anchor").click()
}}function D(){N();
I()
}return{setupAnchors:D,updateNotificationCount:G}
}();
if(AJS.I18n&&AJS.I18n.keys){AJS.I18n.keys["com.atlassian.mywork.keyboard.shortcut.open.notifications.desc"]="Offene Benachrichtigungen"
}MW.MV.AnchorManager.setupAnchors();
anchorUtil=new MW.AnchorUtil(A,contextPath,MW.MV.AnchorManager.updateNotificationCount);
anchorUtil.setupAnchors();
A("#notifications-anchor").click(function(){MW.MV.AnchorManager.updateNotificationCount(0,"0")
});
A(window).focus(function(){console.log("Focus - starting requests");
anchorUtil.startRequests()
});
A("body").click(function(){anchorUtil.startRequests()
})
});
var MW=MW||{};
MW.AnchorUtil=function(D,K,E){var I=30000,S,P=K+"/rest/mywork/latest/status/notification/count";
var R=new Date().getTime();
var B=5*60*1000;
var T=1000*60*5;
var G=1.25;
var C=0;
function H(U){return U<=9?U:"9+"
}function Q(){window.clearInterval(S);
S=undefined
}function O(){return(new Date().getTime()-R)<T
}function M(){if(!O()||!S){L()
}R=new Date().getTime()
}function L(){if(S){clearTimeout(S)
}S=setTimeout(function(){F()
},C=N(C))
}function A(W,U){var V=W*1000;
B=U*1000||B;
if(V&&V!=I){I=V;
M()
}}function N(U){return Math.min(O()?I:U*G,B)
}function F(){console.log("Updating anchors");
D.getJSON(P,function(V){A(V.timeout,V.maxTimeout);
var U=V.count;
E(U,H(U))
});
L()
}function J(){F();
M()
}return{setupAnchors:J,startRequests:M,stopRequests:Q}
};
