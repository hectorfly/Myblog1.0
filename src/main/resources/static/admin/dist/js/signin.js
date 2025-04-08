$(function () {
    //验证
    $('#signin').click(function () {
        var originalPassword = $('#password').val();
        var newPassword = $('#password2').val();
        var form = $('#signinform').val();
        var mail = $('#mail').val();
        var nickName = $('#nickName').val();

        if (validPasswordForUpdate(originalPassword, newPassword,mail,nickName)) {
            form.submit;
        } else {
            $("#signin").attr("disabled",false);
        }
    });

})




/**
 * 密码验证
 */
function validPasswordForUpdate(originalPassword, newPassword,mail,nickName) {
    if (isNull(nickName) || nickName.trim().length < 1) {
        $('#updatePassword-info').css("display", "block");
        $('#updatePassword-info').html("请输入名称！");
        return false;
    }
    if (isNull(mail) || mail.trim().length < 1) {
        $('#updatePassword-info').css("display", "block");
        $('#updatePassword-info').html("请输入邮箱账号！");
        return false;
    }
    if (isNull(originalPassword) || originalPassword.trim().length < 1) {
        $('#updatePassword-info').css("display", "block");
        $('#updatePassword-info').html("请输入密码！");
        return false;
    }
    if (isNull(newPassword) || newPassword.trim().length < 1) {
        $('#updatePassword-info').css("display", "block");
        $('#updatePassword-info').html("确认密码不能为空！");
        return false;
    }
    if (!validPassword(newPassword)) {
        $('#updatePassword-info').css("display", "block");
        $('#updatePassword-info').html("请输入符合规范的密码！需要数字加上字母大小写！");
        return false;
    }
    if (originalPassword != newPassword) {
        $('#updatePassword-info').css("display", "block");
        $('#updatePassword-info').html("新旧密码不符合！");
        return false;
    }
    return true;
}