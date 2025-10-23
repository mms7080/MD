package com.example.demo.portfolios;

import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.repository.PortfoliosRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioDataInitializer implements CommandLineRunner{
    private final PortfoliosRepository repository;

    @Override
    public void run(String... args){
        if(repository.count() > 0) return;

        // Proxy Server
        PortfoliosEntity proxyServer = new PortfoliosEntity();
        proxyServer.setTitle("PorxyServer");
        proxyServer.setDesc("proxyServer");
        proxyServer.setCover("/images/proxyServer/p_1.png");
        proxyServer.setIcon("/images/proxyServer/p_icon.png");
        proxyServer.setScreenshots(List.of(
            "/images/proxyServer/p_1.png",
            "/images/proxyServer/p_2.png"
        ));
        proxyServer.setTags(Set.of("JavaScript","VSCODE","NodeJS","Express","FS"));
        proxyServer.setTeamName("ProxyServer");
        proxyServer.setLink("https://kafolio.kr");
        proxyServer.setViewCount(0);



    }
    
}
