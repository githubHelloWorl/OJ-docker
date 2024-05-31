package com.example.czojtest.security;

import java.security.Permission;

public class MySecurityManager extends SecurityManager{
    /**
     * 检查所有的权限
     * @param perm   the requested permission.
     */
    @Override
    public void checkPermission(Permission perm){
        System.out.println("默认不做任何限制");
//        super.checkPermission(perm);
//        throw new SecurityException("权限不足" + perm.getActions());
    }

    // 检查程序是否可执行文件
    @Override
    public void checkExec(String cmd) {
//        super.checkExec(cmd);
        System.out.println(cmd);
        throw new SecurityException("checkExec 权限异常" + cmd);
    }

    // 检测程序是否可以读文件
    @Override
    public void checkRead(String file) {
//        super.checkRead(file);
        System.out.println(file);
        if(file.contains("C:\\code")){
            return;
        }
        throw new SecurityException("checkRead 权限异常" + file);
    }

    // 检测程序是否可以写文件
    @Override
    public void checkWrite(String file){
        super.checkWrite(file);
    }

    // 检测程序是否可以删除文件
    @Override
    public void checkDelete(String file) {
//        super.checkDelete(file);
        throw new SecurityException("checkDelete 权限异常" + file);
    }

    // 检测程序是否可以连接
    @Override
    public void checkConnect(String host, int port) {
        super.checkConnect(host, port);
    }

}
