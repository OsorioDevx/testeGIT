#!/usr/bin/env bash
# Compila e executa o projeto (Linux / macOS)
set -e
cd "$(dirname "$0")"
rm -rf out
# -sourcepath src faz o javac achar e compilar sozinho todas as classes usadas pelo Main
javac -encoding UTF-8 -d out -sourcepath src src/br/com/banco/Main.java
java -cp out br.com.banco.Main
