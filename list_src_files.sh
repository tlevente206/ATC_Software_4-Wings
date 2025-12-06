#!/bin/bash

set -euo pipefail

# A script futtatási helye (projekt gyökere)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$SCRIPT_DIR/src"

# Kimeneti fájl
OUTPUT_FILE="$SCRIPT_DIR/project_sources.txt"

# Ürítjük / létrehozzuk a kimeneti fájlt
: > "$OUTPUT_FILE"

# Csak engedélyezett kiterjesztések
ALLOWED_EXTENSIONS=(
  "*.java"
  "*.fxml"
  "*.css"
  "*.xml"
  "*.properties"
)

echo "Keresés itt: $SRC_DIR"
echo "Engedélyezett fájlok: ${ALLOWED_EXTENSIONS[*]}"
echo "Kimeneti fájl: $OUTPUT_FILE"
echo

# Find parancs összeállítása
FIND_CMD=(find "$SRC_DIR" -type f \( )
FIRST=1
for ext in "${ALLOWED_EXTENSIONS[@]}"; do
  if [ $FIRST -eq 1 ]; then
    FIND_CMD+=(-name "$ext")
    FIRST=0
  else
    FIND_CMD+=(-o -name "$ext")
  fi
done
FIND_CMD+=( \) )

# Fájlok bejárása
"${FIND_CMD[@]}" -print0 | while IFS= read -r -d '' file; do
  {
    echo "========== FILE START =========="
    echo "PATH: $file"
    echo "================================"
    cat "$file"
    echo
    echo "=========== FILE END ==========="
    echo
  } >> "$OUTPUT_FILE"
done

echo "KÉSZ! A fájl itt található:"
echo "$OUTPUT_FILE"