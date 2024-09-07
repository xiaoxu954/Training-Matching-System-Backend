package com.xiaoxu.controller;

import com.jfinal.ext.cors.EnableCORS;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import io.jboot.web.controller.JbootController;
import io.jboot.web.controller.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;


@EnableCORS
@RequestMapping("/")
public class IndexController extends JbootController {

    public void index() {
        getResponse().setHeader("Access-Control-Allow-Origin", "*");
        renderText(String.valueOf(Ret.ok("data", "Hello World Jboot")));
    }

    public void dbtest() {
        List<Record> records = Db.find("select * from user");
        renderJson(Arrays.toString(records.toArray()));
    }

}