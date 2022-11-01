create table pins
(
    id uuid not null 
        constraint pins_pkey primary key,
    pin_type varchar(255) not null,
    title varchar(255),
    text text,
    sender varchar(255),
    original text
);

create table investigations
(
    id uuid not null 
        constraint investigations_pkey primary key,
    title varchar(255) not null
);

create table if not exists investigations_pins
(
    investigation_id uuid not null 
        constraint investigations_fk references investigations,
    pin_id uuid not null 
        constraint pins_fk references pins,
    constraint investigations_pins_pkey primary key (investigation_id, pin_id)
);

insert into investigations(id, title) values ('eb376c53-a190-45ca-8f66-cd0c95beacb7', '2022 Barcelona');
insert into investigations(id, title) values ('eb376c53-a190-45ca-8f66-cd0c95beacb8', '2022 France');

insert into pins (id, pin_type, sender, text) values ('a2c39161-747c-4dd6-b268-6992e53909e9', 'TELEGRAM_MESSAGE', 'Alice', 'Hello');
insert into pins (id, pin_type, sender, text) values ('a2c39161-747c-4dd6-b268-6992e53909e0', 'TELEGRAM_MESSAGE', 'Bob', 'Hi');

insert into investigations_pins (investigation_id, pin_id) values ('eb376c53-a190-45ca-8f66-cd0c95beacb7', 'a2c39161-747c-4dd6-b268-6992e53909e9');
insert into investigations_pins (investigation_id, pin_id) values ('eb376c53-a190-45ca-8f66-cd0c95beacb7', 'a2c39161-747c-4dd6-b268-6992e53909e0');
