package com.container.example;

import com.container.example.model.ExampleDto;
import com.container.example.service.SecondDependency;
import com.container.example.service.TopLevelService;
import com.container.example.util.Container;

public class Main {
    private static final Container container = Container.getContainer();

    public static void main(String[] args) {
        try {
            TopLevelService topLevelService = (TopLevelService) container.getInstance(TopLevelService.class);
            ExampleDto example = topLevelService.getExample();
            SecondDependency secondDependency = (SecondDependency) container.getInstance(SecondDependency.class);
            System.out.println(secondDependency.getPrintName());
            System.out.println(example);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
