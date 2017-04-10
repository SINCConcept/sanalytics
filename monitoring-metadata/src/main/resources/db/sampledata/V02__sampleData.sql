insert into Slice(id, graph_Definition) values ('example1', STRINGDECODE('graph LR\nA --> B\nB --> C'));
insert into Slice(id, graph_Definition) values ('example2', STRINGDECODE('graph LR\n\nA --> B\nA --> C'));
insert into Slice(id, graph_Definition) values ('example3', STRINGDECODE('graph LR\n\nA --> B\nC --> B'));