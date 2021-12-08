package com.symphony.bdk.core.activity.parsing;

import org.apiguardian.api.API;

@API(status = API.Status.INTERNAL)
public class MentionInputToken implements InputToken<Mention> {

  private Mention mention;

  public MentionInputToken(String mentionText, Long userId) {
    this.mention = new Mention(mentionText, userId);
  }

  @Override
  public Mention getContent() {
    return mention;
  }

  @Override
  public String getContentAsString() {
    return mention.getText();
  }
}