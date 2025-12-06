#!/bin/bash

OUTPUT="filelist.txt"

# Előző lista törlése
rm -f "$OUTPUT"

# Fájlok végigjárása
find src -type f | while read -r FILE; do
    echo "==========================================" >> "$OUTPUT"
    echo "FILE: $FILE" >> "$OUTPUT"
    echo "==========================================" >> "$OUTPUT"
    echo "" >> "$OUTPUT"

    # Tartalom hozzáfűzése
    cat "$FILE" >> "$OUTPUT"

    echo "" >> "$OUTPUT"
    echo "" >> "$OUTPUT"
done

echo "Kész! -> $OUTPUT"