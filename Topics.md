# 9日(以降)のnightlyでNAITO-Rescue10を起動する方法 #

SERVER\_TOP\_DIR/jars, SERVER\_TOP\_DIR/lib内にあるすべてのjarファイルを，
naito\_rescue/jarsディレクトリにコピーする．

逆に，9日以前のnightlyで起動する場合にも，そのnightlyのjars, lib内にある
すべてのjarファイルをコピーしておく必要があります．

# 9日付けのnightlyでNAITO-Rescue10を起動したときに「Exception in readInt8〜」などと大量に表示されてエージェントが落ちる時の対処法 #

SERVER\_TOP\_DIR/boot/config/comms.cfgを以下のように編集します

**before**

#!include comms/comms-01-full.cfg
...

**after(1行目のコメントアウトを外す)**

!include comms/comms-01-full.cfg

現在のNAITO-Rescue10が，自動的に無線チャネル1を使う設定になっているのですが，
9日付のnightlyでは，デフォルトで無線チャネル1を使用できない設定になっているために起こる現象です．
コードの改良で対処いたします．

# Details #

Add your content here.  Format your content with:
  * Text in **bold** or _italic_
  * Headings, paragraphs, and lists
  * Automatic links to other wiki pages