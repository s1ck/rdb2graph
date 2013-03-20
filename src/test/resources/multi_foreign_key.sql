use demo;

drop table if exists A;
drop table if exists B;

create table A (
    idA int not null,
    idB1 int not null,
    idB2 int not null,
    content varchar(25),
    primary key (idA)
);

create table B (
    idB1 int not null,
    idB2 int not null,
    content varchar(25),
    primary key (idB1, idB2)
);

alter table A add constraint fk_B
    foreign key (idB1, idB2)
    references B(idB1, idB2);

insert into B (idB1, idB2, content) values (1,1, '1-1');
insert into B (idB1, idB2, content) values (1,2, '1-2');
insert into B (idB1, idB2, content) values (2,1, '2-1');
insert into B (idB1, idB2, content) values (2,2, '2-2');

insert into A (idA, idB1, idB2, content) values (1, 1, 1, '1-1-1');
insert into A (idA, idB1, idB2, content) values (2, 1, 2, '2-1-2');
insert into A (idA, idB1, idB2, content) values (3, 2, 1, '3-2-1');
insert into A (idA, idB1, idB2, content) values (4, 2, 2, '4-2-2');





    