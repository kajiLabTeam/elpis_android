
### linterについて

このプロジェクトはktlintを用いて静的コード解析を行なっている。

コードは以下の二つがある適宜PRを出す前にチェックをすること。
```bash
# 自動でフォーマットをかける
 ./gradlew ktlintFormat

# コードのルール違反をチェックする
./gradlew ktlintCheck 
```