package com.container.example.service.impl;

import com.container.example.annotation.Component;
import com.container.example.service.FirstDependency;
import com.container.example.service.SecondDependency;

@Component
public class FirstDependencyImpl implements FirstDependency {
    private final SecondDependency secondDependencyImpl;

    public FirstDependencyImpl(SecondDependency secondDependencyImpl) {
        this.secondDependencyImpl = secondDependencyImpl;
    }

    @Override
    public String getFullName() {
        return "FirstLevelDependencyName ".concat(secondDependencyImpl.getPrintName());
    }
}
