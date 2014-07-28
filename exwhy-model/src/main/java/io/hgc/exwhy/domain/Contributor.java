package io.hgc.exwhy.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;

@Entity
public class Contributor {
    @Id
    private int id;

    @Size(max = 2048)
    private String openId;

    @Size(max = 24)
    private String username;

    @Size(max = 255)
    private String realName;

    @Size(max = 255)
    private String email;

    @NotNull
    private ZonedDateTime joined;
}