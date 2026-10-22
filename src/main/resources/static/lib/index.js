/**

 @Name：layuiAdmin iframe版主入口
 @Author：贤心
 @Site：http://www.layui.com/admin/
 @License：LPPL
    
 */
 
layui.extend({
  setter: 'config' //配置模块
  ,admin: 'lib/admin' //核心模块
  ,view: 'lib/view' //视图渲染模块
}).define(['setter', 'admin'], function(exports){
  var setter = layui.setter
  ,element = layui.element
  ,admin = layui.admin
  ,tabsPage = admin.tabsPage
  ,view = layui.view

  //打开标签页
  ,openTabsPage = function(url, text){
    //遍历页签选项卡
    var matchTo
    ,tabs = $('#LAY_app_tabsheader>li')
    ,path = url.replace(/(^http(s*):)|(\?[\s\S]*$)/g, '');
    
    tabs.each(function(index){
      var li = $(this)
      ,layid = li.attr('lay-id');
      
      if(layid === url){
        matchTo = true;
        tabsPage.index = index;
      }
    });
    
    text = text || '新标签页';
    
    if(setter.pageTabs){
      //如果未在选项卡中匹配到，则追加选项卡
      if(!matchTo){
        $(APP_BODY).append([
          '<div class="layadmin-tabsbody-item layui-show">'
            ,'<iframe src="'+ url +'" frameborder="0" class="layadmin-iframe"></iframe>'
          ,'</div>'
        ].join(''));
        tabsPage.index = tabs.length;
        element.tabAdd(FILTER_TAB_TBAS, {
          title: '<span>'+ text +'</span>'
          ,id: url
          ,attr: path
        });
      }
    } else {
      var iframe = admin.tabsBody(admin.tabsPage.index).find('.layadmin-iframe');
      iframe[0].contentWindow.location.href = url;
    }

    //定位当前tabs
    element.tabChange(FILTER_TAB_TBAS, url);
    admin.tabsBodyChange(tabsPage.index, {
      url: url
      ,text: text
    });
  }
  
  ,APP_BODY = '#LAY_app_body', FILTER_TAB_TBAS = 'layadmin-layout-tabs'
  ,$ = layui.$, $win = $(window);

  //全局请求统一携带 token（请求头 x-66nao-auth-token），并统一处理登录失效
  $(document).ajaxSend(function(event, xhr, settings){
    var token = setter.request.tokenName ? layui.data(setter.tableName)[setter.request.tokenName] : '';
    if(token){
      xhr.setRequestHeader(setter.request.tokenName, token);
    }
  });
  $(document).ajaxSuccess(function(event, xhr, settings){
    var res = xhr.responseJSON;
    if(res && res[setter.response.statusName] === setter.response.statusCode.logout){
      //登录失效：清除本地 token，跳转登录页
      layui.data(setter.tableName, {
        key: setter.request.tokenName
        ,remove: true
      });
      location.href = '/';
    }
  });
  
  //初始
  if(admin.screen() < 2) admin.sideFlexible();
  
  //将模块根路径设置为 controller 目录
  layui.config({
    base: setter.base + 'modules/'
  });
  
  //扩展 lib 目录下的其它模块
  layui.each(setter.extend, function(index, item){
    var mods = {};
    mods[item] = '{/}' + setter.base + 'lib/extend/' + item;
    layui.extend(mods);
  });
  
  view().autoRender();
  
  //加载公共模块
  layui.use('common');

  // ===== 弹窗(iframe)数据传递 =====
  // layer 的 type:2 弹窗是在 iframe 的 load 事件之后才调用 success，而子页面脚本通常先于
  // 该时机读取 window.passData / window.type / window.bizType，会取到 undefined
  // (表现为「同步数据显示 undefined」「环境未默认选中」「编辑弹窗字段为空」)。
  // 约定：父页面在 layer.open 之前调用 layDialog.stash(data, type, bizType)；
  //       子页面统一用 layDialog.get('passData' | 'type' | 'bizType') 读取。
  window.layDialog = layui.layDialog = {
    // 父页面：打开弹窗前把数据挂到父窗口，供子页面在其脚本执行期间读取
    // opts: {data: 传给子页面的数据(JSON 字符串或对象), type: 环境标识, bizType: 业务标识}
    stash: function(opts){
      window.__layDialogData = opts || {};
    },
    // 子页面：优先取 layer 回填到本窗口的值，尚未回填时回退父窗口暂存值
    get: function(key){
      if(window[key] !== undefined) return window[key];
      var stash = (window.parent && window.parent.__layDialogData) || {};
      return key === 'passData' ? stash.data : stash[key];
    }
  };

  //对外输出
  exports('index', {
    openTabsPage: openTabsPage
  });
});
