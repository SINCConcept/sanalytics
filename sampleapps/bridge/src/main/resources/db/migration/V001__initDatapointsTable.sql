
create table Datapoint (
   station varchar(255),
   datapoint varchar(255),
   val double,
   primary key(station, datapoint)
);
