package io.hgc.exwhy.domain;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;

@Entity
//@PrimaryKeyJoinColumn(name = "item_id")
public class Post extends Item {
    @ManyToOne
    private Contributor contributor;

    @Size(max = 56)
    private String title;

    @Lob
    private String body;

    private ZonedDateTime posted;
}