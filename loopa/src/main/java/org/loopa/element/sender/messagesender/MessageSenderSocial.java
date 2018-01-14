package org.loopa.element.sender.messagesender;

import org.loopa.comm.message.IMessage;
import org.loopa.externalservice.ExternalService;

public class MessageSenderSocial extends MessageSender {
  @Override
  protected void sendMessage(IMessage message) {
    ExternalService externalService = (ExternalService) this.getComponent().getComponentRecipients().get(message.getMessageTo());
    externalService.processRequest(message);
  }
}
