######################################
#    NAITO-Rescue10ベースコード
#
#                 2010.4.27 Shoji Kazuo
#
#    地下鉄の中で，500mlのペットボトルで
#    自分の頭を叩き続ける超美形の女性を見た．
#    (しかもすごい笑顔)
#
#    「あぁ冷や汗ってこういうときにかくんだ」
#    ということを理解した．
######################################

対象: RoboCupRescueSimulation 1.0 nightly-build(2010.4.26日付まで)

ビルドに必要なもの:

      (1) サンマイクロシステムズのJava Compiler 1.6以上
	    (およびそれに付属するjarコマンド)
      (2) ant 1.7.0
	    (antが使うjarファイルの揃い具合によっては，
		より古いantでもビルドが可能かもしれません)
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

      (1) "start.sh"ファイルの中にある
	          ・KERNEL_ADDRESS
		      ・KERNEL_PORT
		  という2つの変数を環境に合わせて書き換える
		  (KERNEL_ADDRESS ... 接続するサーバのIPアドレス
		   KERNEL_PORT    ... 接続するサーバのポート番号)
      (2) start.shを実行する
	        $./start.sh
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
	こうしないと，作ったエージェントのクラスをカーネルが自動的に認識してくれないため．

