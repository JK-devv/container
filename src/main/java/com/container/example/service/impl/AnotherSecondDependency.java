package com.container.example.service.impl;

import com.container.example.annotation.Component;
import com.container.example.service.SecondDependency;

@Component
public class AnotherSecondDependency implements SecondDependency {
    @Override
    public String getPrintName() {
        return "another second";
    }
}
