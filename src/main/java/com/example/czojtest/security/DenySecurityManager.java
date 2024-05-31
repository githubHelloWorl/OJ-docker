package com.example.czojtest.security;

import java.security.Permission;

public class DenySecurityManager extends SecurityManager{

    /**
     * 拒绝所有的权限
     * @param perm   the requested permission.
     */
    @Override
    public void checkPermission(Permission perm){
        throw new SecurityException("权限不足" + perm.getActions());
    }
}
