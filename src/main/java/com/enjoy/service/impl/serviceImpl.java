package com.enjoy.service.impl;

import com.enjoy.annotion.Service;
import com.enjoy.service.MyService;
@Service("Myservice")
public class serviceImpl implements MyService {
    @Override
    public String query1(String name, String age) {
        return null;
    }

    @Override
    public String query(String name, String age) {
        return name+"===="+age;
    }
}
