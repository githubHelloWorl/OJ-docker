package com.example.czojtest.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PingCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;
public class DockerDemo {
    public static void main(String[] args) throws InterruptedException {
        // 获得默认的 Docker Client
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        // PingCmd pingCmd = dockerClient.pingCmd();
        // pingCmd.exec();
//        String image = "nginx:latest";
        String image= "nginx";
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem pullResponseItem){
                System.out.println("下载镜像: " + pullResponseItem.getStatus());
                super.onNext(pullResponseItem);
            }
        };

        pullImageCmd
                .exec(pullImageResultCallback)
                .awaitCompletion();



        System.out.println("下载完成");
    }
}
