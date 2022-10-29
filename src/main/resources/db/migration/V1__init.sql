create table pins
(
    id uuid not null constraint pins_pkey primary key,
    `type` varchar(255) not null
);

create table investigations
(
    id uuid not null constraint investigations_pkey primary key,
    title varchar(255) not null
);
