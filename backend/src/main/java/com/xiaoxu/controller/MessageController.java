package com.xiaoxu.controller;

import com.jfinal.aop.Inject;
import com.jfinal.ext.cors.EnableCORS;
import com.jfinal.kit.Ret;
import com.xiaoxu.commom.DeleteRequest;
import com.xiaoxu.commom.ErrorCode;
import com.xiaoxu.exception.BusinessException;
import com.xiaoxu.model.dto.message.MessageQueryRequest;
import com.xiaoxu.model.vo.InteractionMessageVO;
import com.xiaoxu.model.vo.MessageVO;
import com.xiaoxu.service.MessageService;
import io.jboot.web.controller.JbootController;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jboot.web.json.JsonBody;

import java.util.List;

@EnableCORS(allowOrigin = "http://localhost:3002")
@RequestMapping("/api/message")
public class MessageController extends JbootController {

    @Inject
    private MessageService messageService;


    public void interactionList() {
        InteractionMessageVO interactionMessageVO = messageService.listInteractionMessage(getRequest());

        renderJson(Ret.ok("data", interactionMessageVO));
    }


    public void MessageList(@JsonBody MessageQueryRequest messageQueryRequest) {

        if (messageQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<MessageVO> messageVOList = messageService.listMessages(messageQueryRequest, getRequest());
        renderJson(Ret.ok("data", messageVOList));
    }

    public void deleteMessage(@JsonBody DeleteRequest deleteRequest) {
        if (deleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = messageService.deleteById(deleteRequest.getId());
        renderJson(Ret.ok("data", b));

    }

}
