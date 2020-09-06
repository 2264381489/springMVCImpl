package com.enjoy.controller;

import com.enjoy.annotion.AutoWired;
import com.enjoy.annotion.EnjoyController;
import com.enjoy.annotion.RequestMapping;
import com.enjoy.annotion.RequestParam;
import com.enjoy.service.MyService;

@EnjoyController("MyController")
@RequestMapping("/james")
public class MyController {
    @AutoWired("Myservice")
    private MyService service;
    @RequestMapping("/query")
    public void query( @RequestParam("name")String name,@RequestParam("age") String age){
        String res = service.query(name, age);
        System.out.println(res);

    }




}
