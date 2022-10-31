create table pins
(
    id uuid not null 
        constraint pins_pkey primary key,
    type varchar(255) not null
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
