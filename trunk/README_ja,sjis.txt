######################################
#    NAITO-Rescue10ベースコード
#
#                 2010.4.1 Shoji Kazuo
#
#    「庄子」の姓は，中国では尊い名前らしい．
#     ...が，この性は宮城，山形にいっくらでも存在する．
######################################

対象: RoboCupRescueSimulation 1.0 nightly build(2010.3.18)

ビルドに必要なもの:

    Java Compiler 1.6以上(およびそれに付属するjarコマンド)
	Makefileを認識して動作するシステム

ビルドおよび動作確認:

    1) makeする(コンパイルの詳細はMakefile参照)
     ####################
     # 各自の環境に合わせて，
     # Makefile内のKERNEL_BASE変数を
     # 書き換えてください．
     # (KERNEL_BASE変数には，
     # 使用するサーバのトップディレクトリ
     # へのパスを記述します)
     ####################
	2) naito_rescue.jarというファイルができているので，
	それを%SERVER_TOP_DIRECTORY%/jarsに放り込む．
	3) %SERVER_TOP_DIRECTORY%/boot/configにあるkernel.cfg
	を以下のように編集する．
	   #kernel.agents.auto +: ... の行についてコメントアウトを
	   外し，以下のように書き換える．

	   kernel.agents.auto +: naito_rescue.agent.NAITOFireBrigade*n
	   kernel.agents.auto +: naito_rescue.agent.NAITOAmbulanceTeam*n
	   kernel.agents.auto +: naito_rescue.agent.NAITOPoliceForce*n

    4) ant start-kernelでサーバを起動し，スタートの画面で
	NAITOFireBrigade,NAITOAmbulanceTeam,NAITOPoliceForceが登録されていること
	を確認する．

クラス構成:


	NAITOAgent
        |__NAITOHumanoidAgent
        |       |__NAITOFireBrigade
        |       |__NAITOAmbulanceTeam
        |       |__NAITOPoliceForce
        |       |
		|       |...<Civilian>
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

今後の予定:

    とりあえずTask-Job Systemは組み込みたい(エージェントの行動を組み立てやすい，
	行動の終了判定がしやすい，行動の開始時間と終了時間などのデータを取りやすいなど
	の理由より)．
	戦略はどうしよう＼(^0^)/
