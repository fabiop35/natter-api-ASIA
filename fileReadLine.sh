#!/bin/bash
#Francesco Foschi length of a row in a file

let n=0
let max_length=0

while read row
do
    length=$(echo -n $row | wc -c)
    if [ ${length} -gt ${max_length} ]
    then
            let max_length=${length}
    fi

    echo "$n ROW is $length charachters long"
    echo "$row"
    let n=n+1

done < $1

echo "longest line is $max_length charachters long"

exit 0
