package com.example.czojtest;


import com.example.czojtest.model.ExecuteCodeRequest;
import com.example.czojtest.model.ExecuteCodeResponse;

public interface CodeSandbox {

    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
