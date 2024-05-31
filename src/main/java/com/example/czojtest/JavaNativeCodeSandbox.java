package com.example.czojtest;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.example.czojtest.model.ExecuteCodeRequest;
import com.example.czojtest.model.ExecuteCodeResponse;
import com.example.czojtest.model.ExecuteMessage;
import com.example.czojtest.model.JudgeInfo;
import com.example.czojtest.security.DefaultSecurityManager;
import com.example.czojtest.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class JavaNativeCodeSandbox implements CodeSandbox{

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_CODE_CLASS_NAME = "Main.java";

    private static final Long TIME_OUT = 10000L;

    private static final String SECURITY_MANAGER_PATH = "E:\\practice\\experment\\OJ\\OJ-Backend\\cz-oj\\src\\main\\java\\com\\example\\czojsandbox\\security";

    private static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";

    private static final List<String> blackList = Arrays.asList("Files","exec");

    private static final WordTree WORD_TREE;

    static {
        // 初始化字典树
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(blackList);
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest){
        // 设置默认安全组
        System.setSecurityManager(new DefaultSecurityManager());

        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        // 校检代码 如果是否包含黑名单命令
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if(foundWord != null){
            System.out.println("包含敏感词:" + foundWord.getFoundWord());
            return null;
        }
//        WordTree wordTree = new WordTree();
//        wordTree.addWords(blackList);
//        FoundWord foundWord = wordTree.matchWord(code);
//        if(StrUtil.isNotBlank(foundWord.getFoundWord())){
//            System.out.println(foundWord.getFoundWord());
//            return null;
//        }


        // 1) 将提交的代码保存到文件中
//        List<String> inputList = executeCodeRequest.getInputList();
//        String code = executeCodeRequest.getCode();

        // 得到当前用户的工作目录
        String userDir = System.getProperty("user.dir");
        String golbalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        if(FileUtil.exist(golbalCodePathName)){
            FileUtil.mkdir(golbalCodePathName);
        }

        // 把用户的代码隔离存放
        String userCodeParentPath = golbalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_CODE_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        // 2) 编译代码,得到 class 文件
        String complieCmd = String.format("javac -encoding utf-8 %s", userCodePath);
        try {
            Process process = Runtime.getRuntime().exec(complieCmd);

            // 使用工具类
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(process, "编译");
            System.out.println(executeMessage);
        } catch (IOException e) {
            return getErrorResponse(e);
//            throw new RuntimeException(e);
        }

        // 3 执行代码,得到输出结果
        List<ExecuteMessage> executeMessagesList = new ArrayList<>();
        for(String inputArgs : inputList){
            StopWatch stopWath = new StopWatch();
//            String runCmd = String.format("java -Xmx4096m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath);
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 %s;%s -Djava.security.manager=%s Main %s", userCodeParentPath,SECURITY_MANAGER_PATH,SECURITY_MANAGER_CLASS_NAME, inputArgs);

            // 限制时间
            try {
                stopWath.start();
                Process runProcess = Runtime.getRuntime().exec(runCmd);

                // 超时控制
                new Thread(() -> {
                    try{
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时了,中断");
                        runProcess.destroy();
                    }catch(InterruptedException e){
                        throw new RuntimeException(e);
                    }
                }).start();

                // 使用工具类
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                System.out.println(executeMessage);
                executeMessagesList.add(executeMessage);
            } catch (IOException e) {
                return getErrorResponse(e);
//                throw new RuntimeException(e);
            }
        }

        // 4 收集整理输出结果
        // 取时最大值,判断是否超时
        Long maxTime = 0L;
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        for(ExecuteMessage executeMessage:executeMessagesList){
            String errorMessage = executeMessage.getErrorMessage();
            if(StrUtil.isNotBlank(executeMessage.getErrorMessage())){
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if(time!=null){
                maxTime = Math.max(maxTime, time);
            }
        }

        // 正常执行完成
        if(outputList.size() == executeMessagesList.size()){
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        // 要借助第三方库来实现,非常麻烦,此处不作处理
//        judgeInfo.setMemory();
        executeCodeResponse.setJudgeInfo(judgeInfo);

        // 5 文件清理
        if(userCodeFile.getParentFile() != null){
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功":"失败"));
        }


        // 6 错误处理

        return executeCodeResponse;
    }

    /**
     * 返回错误响应
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        // 表示代码沙箱错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
