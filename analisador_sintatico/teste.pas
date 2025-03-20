program repete;
var
    c: integer;
begin
    c := 1;
    repeat
        write(c);
        c := c + 1;
    until (c > 10);
end.
