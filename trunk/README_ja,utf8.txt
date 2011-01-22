######################################
#    NAITO-Rescue10ベースコード
#
#                 2010.11.11 Shoji Kazuo
#
#   今日はポッキーの日
#   
######################################

対象: RoboCupRescueSimulation 1.0 nightly-build(2010.5.10日付まで)


ビルドに必要なもの:

      (1) サンマイクロシステムズのJava Compiler 1.6以上
	    (およびそれに付属するjarコマンド)
      (2) ant 1.7.1以上
	    (antが使うjarファイルの揃い具合によっては，
		より古いantでもビルドが可能かもしれません)
		(Ubuntu10.04なら，普通に入れたantで大丈夫
		です)
	  (3) GNU Makefile

ビルドおよび実行について:

  1. ビルド
      (1) Makefileのあるディレクトリでmakeを実行する
	        $make
      (2) naito_rescue.jarというファイルができている
  	      ことを確認する

  2. 実行
   2-A. エージェントからサーバにつなげる方法
        (通常はこちらの起動方法になると思います)
      (0) サーバを立ち上げます(ここに書いてある方法が全てとは限りません．
	      シミュレーションをシェルスクリプトから立ち上げる方法もあります)
          (0-1) サーバが置かれているディレクトリの
                    boot/config/kernel-inline.cfg
                を開き，
                    kernel.agents.auto
                で始まる行をすべてコメントアウトします．
          (0-2) サーバを起動します
                    $ant start-kernel
          (0-3) "Setup kernel options"というタイトルの画面が出てきたら，
                "Agents"と書かれた枠のなかを見て，どこにもチェックが入っ
                ていないことを確認します．OKボタン(環境によっては「了解」
                ボタン)を押します．
          (0-4) "Kernel GUI"というタイトルの画面が出てきたら，
                "Agents"と書かれた枠の中に何も表示されていないことを確認
                します．
                その後，下の(1)以降を実行してください．
      (1) "go.sh"ファイルの中にある
	          ・KERNEL_ADDRESS
		      ・KERNEL_PORT
		  という2つの変数を環境に合わせて書き換える
		  (KERNEL_ADDRESS ... 接続するサーバのIPアドレス
		   KERNEL_PORT    ... 接続するサーバのポート番号)
      (2) go.shを実行する
	           $./go.sh
	      "Kernel GUI"の画面にある"Agents"の中に，
	           naito_rescue.NAITOFireBrigade
	           naito_rescue.NAITOAmbulanceTeam
	           naito_rescue.NAITOPoliceForce
	      と出てくれば接続は成功です．Runボタンを押してシミュレーションを
	      開始しましょう．
  2-B. エージェントのjarファイルをサーバから
       自動的に読み取ってもらう方法
	   (こちらは，サーバと同じマシン内にエージェントの
	   jarファイルを置かないと起動できません)
    
	   (1) naito_rescue.jarファイルを，
           %SERVER_TOP_DIRECTORY%/jarsに放り込む．
		   (%SERVER_TOP_DIRECTORY% ... 使用するサーバの
		   トップディレクトリ)
	   (2) %SERVER_TOP_DIRECTORY%/boot/configにあるkernel.cfg
	       を以下のように編集する．

	   #kernel.agents.auto +: ... の行についてコメントアウトを
	   外し，以下のように書き換える．

	   kernel.agents.auto +: naito_rescue.agent.NAITOFireBrigade*n
	   kernel.agents.auto +: naito_rescue.agent.NAITOAmbulanceTeam*n
	   kernel.agents.auto +: naito_rescue.agent.NAITOPoliceForce*n

       (3) ant start-kernelでサーバを起動し，スタートの画面で
	       NAITOFireBrigade,NAITOAmbulanceTeam,NAITOPoliceForceが登録されていること
	       を確認する．

クラス構成:

	NAITOAgent
        |__NAITOHumanoidAgent
        |       |__NAITOFireBrigade
        |       |__NAITOAmbulanceTeam
        |       |__NAITOPoliceForce
        |       |
		|       |...NAITOCivilian
		|...<各種センターエージェント>

    ※<...>は未実装
	NAITOAgent
	    NAITO-Rescueエージェントの基底クラス
	NAITOHumanoidAgent
	    NAITO-Rescueエージェントのうち，人型
		エージェントの基底クラス
	NAITOFireBrigade
	    NAITO-Rescueの消防隊エージェントの実装
		クラス．
		postConnect(),toString(),think(),getRequestedEntityURNsEnum()
		を必ず実装する．
	NAITOAmbulanceTeam
	    NAITO-Rescueの救急隊エージェントの実装
		クラス．
		postConnect(),toString(),think(),getRequestedEntityURNsEnum()
		を必ず実装する．
	NAITOPoliceForce
	    NAITO-Rescueの啓開隊エージェントの実装
		クラス．
		postConnect(),toString(),think(),getRequestedEntityURNsEnum()
		を必ず実装する．
    NAITOCivilian
	    NAITO-Rescueの市民エージェントの実装クラス．
		postConnect(),toString(),think(),getRequestedEntityURNsEnum()
		を必ず実装する．

コーディングの方法:

    エージェントの行動は，各(.+(FireBrigade|AmbulanceTeam|PoliceForce))
	クラスのthink()メソッドに記述する．(think()は，yapAPIのact()に相当する．)
    エージェントが持って来れる情報，とることのできる行動などは，
	サーバに付属するSample(FireBrigade|AmbulanceTeam|PoliceForce)クラス
	を参照のこと．

	[サーバのサンプルエージェント群]:
	%SERVER_TOP_DIRECTORY%/modules/sample/src/sample/Sample(FireBrigade|
	AmbulanceTeam|PoliceForce).java

	[ベースコードを鮮やかに無視して独自のクラスを作る場合]:
	消防隊，救急隊，啓開隊の各クラスについて，クラス名の末尾はかならず
	FireBrigade,AmbulanceTeam,PoliceForceでなければならない．
	こうしないと，作ったエージェントのクラスをカーネルが自動的に認識してくれないため
	(2-Bの起動方法を用いるときに問題が発生するということ)．

(追記)サンプルエージェントについて:
  A. 動作確認
    (1) kernel.cfgについて，「kernel.agents.auto」と書かれている行が
	    以下のようになっていることを確認する．

        kernel.agents.auto +: sample.SampleFireBrigade*n
        kernel.agents.auto +: sample.SamplePoliceForce*n
        kernel.agents.auto +: sample.SampleAmbulanceTeam*n
        kernel.agents.auto +: sample.SampleCentre*n
        kernel.agents.auto +: sample.SampleCivilian*n
        
    (2) サーバを起動する
    
        $ant start-kernel
        
    (3) "Setup kernel options"というタイトルの画面が出てきたら，
        "Agents"と書かれた枠の中を見て，
    
        sample.SampleFireBrigade
        sample.SamplePoliceForce
        sample.SampleAmbulanceTeam
        sample.SampleCentre
        sample.SampleCivilian
        
        と書かれた横の「Maximum」というチェックボックスがONに
        なっていることを確認する．
        その後OK(環境によっては「了解」)ボタンを押す．
    
    (4) 次の画面でRunボタンを押す
    
  B.ソースを読む
      各サンプルエージェントのソースコードは以下にあります．
      %SERVER_TOP_DIRECTORY%/modules/sample/src/sample/SampleFireBrigade.java
      %SERVER_TOP_DIRECTORY%/modules/sample/src/sample/SampleAmbulanceTeam.java
      %SERVER_TOP_DIRECTORY%/modules/sample/src/sample/SamplePoliceForce.java
      %SERVER_TOP_DIRECTORY%/modules/sample/src/sample/SampleCivilian.java
      %SERVER_TOP_DIRECTORY%/modules/sample/src/sample/SampleCentre.java
      
      それぞれのエージェントの行動は，各ソースコードのthinkメソッドの中に書かれています．
      
      あとはクラスの継承関係などを掘り下げていき，ガンガン読んでいきましょう．
