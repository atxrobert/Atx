# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table entry (
  id                        bigint not null,
  position                  integer,
  entry_type                integer,
  text                      varchar(255),
  settings                  varchar(255),
  constraint ck_entry_entry_type check (entry_type in (0,1,2)),
  constraint pk_entry primary key (id))
;

create sequence entry_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists entry;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists entry_seq;

