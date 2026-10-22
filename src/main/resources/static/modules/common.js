/**

 @Name：layuiAdmin 公共业务
 @Author：贤心
 @Site：http://www.layui.com/admin/
 @License：LPPL
    
 */
 
layui.define(function(exports){
  var $ = layui.$
  ,layer = layui.layer
  ,laytpl = layui.laytpl
  ,setter = layui.setter
  ,view = layui.view
  ,admin = layui.admin
  
  //公共业务的逻辑处理可以写在此处，切换任何页面都会执行
  //……
  
  
  
  //退出
  admin.events.logout = function(){
    // 显示确认对话框
    layer.confirm('确定要退出登录吗？', {
      title: '确认退出',
      icon: 3,  // 问号图标
      btn: ['确定', '取消']  // 两个按钮
    }, function(index) {
      // 点击"确定"按钮的回调
      layer.close(index);  // 关闭确认框
      //执行退出接口
      admin.req({
        url: '/api/v1/user/logout'
        ,type: 'get'
        ,data: {}
        ,done: function(res){ //这里要说明一下：done 是只有 response 的 code 正常才会执行。而 succese 则是只要 http 为 200 就会执行
          //清空本地记录的 token，并跳转到登入页
          admin.exit(function(){
            location.href = '/';
          });
        }
      });
    }, function() {
      // 点击"取消"按钮的回调（可选，这里不做操作）
      // layer.msg('已取消退出', {icon: 7});
    });
  };

  
  //对外暴露的接口
  exports('common', {});
});