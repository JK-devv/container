package com.container.example.service.impl;

import com.container.example.annotation.Autowired;
import com.container.example.annotation.Component;
import com.container.example.annotation.PostConstruct;
import com.container.example.service.SecondDependency;

@Component
public class SecondDependencyImpl implements SecondDependency {

    @PostConstruct
    public void doActionOfSecondDepend() {
        System.out.println("bean of secondDependency");
    }

    @Override
    public String getPrintName() {
        return "SecondDependencyPrintName";
    }
}
