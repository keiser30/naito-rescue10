######################################
#    NAITO-Rescue10�x�[�X�R�[�h
#
#                 2010.4.1 Shoji Kazuo
#
#    �u���q�v�̐��́C�����ł͑������O�炵���D
#     ...���C���̐��͋{��C�R�`�ɂ�������ł����݂���D
######################################

�Ώ�: RoboCupRescueSimulation 1.0 nightly build(2010.3.18)

�r���h�ɕK�v�Ȃ���:

    Java Compiler 1.6�ȏ�(����т���ɕt������jar�R�}���h)
	Makefile��F�����ē��삷��V�X�e��

�r���h����ѓ���m�F:

    1) make����(�R���p�C���̏ڍׂ�Makefile�Q��)
     ####################
     # �e���̊��ɍ��킹�āC
     # Makefile����KERNEL_BASE�ϐ���
     # ���������Ă��������D
     # (KERNEL_BASE�ϐ��ɂ́C
     # �g�p����T�[�o�̃g�b�v�f�B���N�g��
     # �ւ̃p�X���L�q���܂�)
     ####################
	2) naito_rescue.jar�Ƃ����t�@�C�����ł��Ă���̂ŁC
	�����%SERVER_TOP_DIRECTORY%/jars�ɕ��荞�ށD
	3) %SERVER_TOP_DIRECTORY%/boot/config�ɂ���kernel.cfg
	���ȉ��̂悤�ɕҏW����D
	   #kernel.agents.auto +: ... �̍s�ɂ��ăR�����g�A�E�g��
	   �O���C�ȉ��̂悤�ɏ���������D

	   kernel.agents.auto +: naito_rescue.agent.NAITOFireBrigade*n
	   kernel.agents.auto +: naito_rescue.agent.NAITOAmbulanceTeam*n
	   kernel.agents.auto +: naito_rescue.agent.NAITOPoliceForce*n

    4) ant start-kernel�ŃT�[�o���N�����C�X�^�[�g�̉�ʂ�
	NAITOFireBrigade,NAITOAmbulanceTeam,NAITOPoliceForce���o�^����Ă��邱��
	���m�F����D

�N���X�\��:


	NAITOAgent
        |__NAITOHumanoidAgent
        |       |__NAITOFireBrigade
        |       |__NAITOAmbulanceTeam
        |       |__NAITOPoliceForce
        |       |
		|       |...<Civilian>
		|...<�e��Z���^�[�G�[�W�F���g>

    ��<...>�͖�����
	NAITOAgent
	    NAITO-Rescue�G�[�W�F���g�̊��N���X
	NAITOHumanoidAgent
	    NAITO-Rescue�G�[�W�F���g�̂����C�l�^
		�G�[�W�F���g�̊��N���X
	NAITOFireBrigade
	    NAITO-Rescue�̏��h���G�[�W�F���g�̎���
		�N���X�D
		postConnect(),toString(),think(),getRequestedEntityURNsEnum()
		��K����������D
	NAITOAmbulanceTeam
	    NAITO-Rescue�̋~�}���G�[�W�F���g�̎���
		�N���X�D
		postConnect(),toString(),think(),getRequestedEntityURNsEnum()
		��K����������D
	NAITOPoliceForce
	    NAITO-Rescue�̌[�J���G�[�W�F���g�̎���
		�N���X�D
		postConnect(),toString(),think(),getRequestedEntityURNsEnum()
		��K����������D

�R�[�f�B���O�̕��@:

    �G�[�W�F���g�̍s���́C�e(.+(FireBrigade|AmbulanceTeam|PoliceForce))
	�N���X��think()���\�b�h�ɋL�q����D(think()�́CyapAPI��act()�ɑ�������D)
    �G�[�W�F���g�������ė������C�Ƃ邱�Ƃ̂ł���s���Ȃǂ́C
	�T�[�o�ɕt������Sample(FireBrigade|AmbulanceTeam|PoliceForce)�N���X
	���Q�Ƃ̂��ƁD

	[�T�[�o�̃T���v���G�[�W�F���g�Q]:
	%SERVER_TOP_DIRECTORY%/modules/sample/src/sample/Sample(FireBrigade|
	AmbulanceTeam|PoliceForce).java

	[�x�[�X�R�[�h��N�₩�ɖ������ēƎ��̃N���X�����ꍇ]:
	���h���C�~�}���C�[�J���̊e�N���X�ɂ��āC�N���X���̖����͂��Ȃ炸
	FireBrigade,AmbulanceTeam,PoliceForce�łȂ���΂Ȃ�Ȃ��D
	�������Ȃ��ƁC������G�[�W�F���g�̃N���X���J�[�l���������I�ɔF�����Ă���Ȃ����߁D

����̗\��:

    �Ƃ肠����Task-Job System�͑g�ݍ��݂���(�G�[�W�F���g�̍s����g�ݗ��Ă₷���C
	�s���̏I�����肪���₷���C�s���̊J�n���ԂƏI�����ԂȂǂ̃f�[�^�����₷���Ȃ�
	�̗��R���)�D
	�헪�͂ǂ����悤�_(^0^)/
