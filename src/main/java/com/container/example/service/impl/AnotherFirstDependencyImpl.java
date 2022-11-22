package com.container.example.service.impl;

import com.container.example.annotation.Component;
import com.container.example.service.FirstDependency;

@Component
public class AnotherFirstDependencyImpl implements FirstDependency {

    @Override
    public String getFullName() {
        return "another full name from anotherFirstDependencyImpl";
    }
}
