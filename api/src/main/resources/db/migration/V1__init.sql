create table pins
(
    id uuid not null 
        constraint pins_pkey primary key,
    created_at timestamp not null,
    pin_type varchar(255) not null,
    title varchar(255),
    text text,
    sender varchar(255),
    file_key uuid,
    html text,
    images varchar(255)[] not null
);

create table investigations
(
    id uuid not null 
        constraint investigations_pkey primary key,
    created_at timestamp not null,
    title varchar(255) not null,
    pins_order uuid[] not null,
    deleted boolean default false not null
);

create table if not exists investigations_pins
(
    investigation_id uuid not null 
        constraint investigations_fk references investigations,
    pin_id uuid not null 
        constraint pins_fk references pins,
    constraint investigations_pins_pkey primary key (investigation_id, pin_id)
);

create table processed_emails
(
    message_id text
        constraint processed_emails_pk
            primary key,
    investigation_ids uuid[] not null
);
