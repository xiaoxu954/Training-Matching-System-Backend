package com.xiaoxu.controller;

import io.jboot.web.controller.JbootController;
import io.jboot.web.controller.annotation.RequestMapping;

@RequestMapping("/websocket")
public class WebsocketController extends JbootController {

    public void index() {
        render("/websocket.html");
    }

}