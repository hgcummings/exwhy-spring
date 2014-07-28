package io.hgc.exwhy.domain;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Item {
    @Id
    private int itemId;

    private int x;

    private int y;
}
