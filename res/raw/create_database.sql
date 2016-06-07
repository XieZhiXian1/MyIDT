#˵����ÿһ��Ҫִ�е���������軻���� / ����
#����ע����(# -- //)
#��һ��Ҫע�⣺С�������µĿո񼰻���

DROP TABLE IF EXISTS sms;
/

#sms_type ��0:all 1:inBox(���۶��Ż��ǵ绰����) 2:sent(��ȥ) 3:draft(�ݸ�) 4:outBox(������) 5:failed 6:queued
#��Ϣ��
create table sms
(
    _id           integer   primary key autoincrement,
    phone_number  text      not null,
    sms_content   text   ,
    sms_type      integer,
    read          integer DEFAULT 0,
    create_time   integer,
    sms_resource_url text,
    sms_resource_type integer,
    sms_resource_name text,
    sms_resource_time_length integer,
    sms_resource_rs_ok integer,
    target_phone_number text      not null,
    owner_phone_number text      not null,
    is_group_message integer,
    uiCause integer DEFAULT -1
);
/
#�Ự��һ���Ự��Ӧһ����¼
create table sms_conversation
(
    _id           integer   primary key autoincrement,
    phone_number  text      not null,
    sms_content   text   ,
    sms_count     integer,
    sms_type      integer,
    read          integer DEFAULT 0,
    create_time   integer,
    sms_resource_url text,
    sms_resource_type integer,
    sms_resource_name text,
    sms_resource_time_length integer,
    sms_resource_rs_ok integer,
    target_phone_number text      not null,
    owner_phone_number text      not null,
    is_group_message integer,
    uiCause integer DEFAULT -1
);
/
#��ϵ�˱�
create table contacts
(
    _id           integer   primary key autoincrement,
    phone_number  text      not null,
	sort_key      text,
    name   		  text   
);
/

#call_type ��1-δ�ӣ�2-���룻3-������4-�ܽӣ�
#���м�¼��
create table call_log
(
    _id           integer   primary key autoincrement,
    contacts_id   integer,
    name   		  text,
    phone_number  text      not null,
    call_type     integer,
    create_time   integer
);
/
#������Ϣ���в���һ�����ݵ�ʱ�򣬲������յ���Ϣ��ʱ��new�������������¼�¼������ɾ��������Ϣ����Ӧ�ĻỰ��¼��Ȼ������sms_conversation�в���һ����Ϣ��������Ϣ���̶��Ϸ�ӳ�����һ�����ݵ����������������롢���ݡ����ࡢ����ʱ�䡢�Ƿ��Ķ�����Ϣ����
CREATE TRIGGER insert_sms_conversation_on_sms_insert 
AFTER INSERT ON sms 
WHEN new.sms_type = 1
BEGIN  
	DELETE FROM sms_conversation WHERE ((sms_type=1 and phone_number = new.phone_number and target_phone_number = new.target_phone_number and owner_phone_number = new.owner_phone_number) or(sms_type!=1 and phone_number = new.target_phone_number and target_phone_number = new.phone_number and owner_phone_number = new.owner_phone_number) or (is_group_message = 1 and target_phone_number = new.target_phone_number));
	INSERT INTO sms_conversation(phone_number,sms_content,sms_type,create_time,read,sms_count,sms_resource_url,sms_resource_type,sms_resource_name,sms_resource_time_length,sms_resource_rs_ok,target_phone_number,owner_phone_number,is_group_message,uiCause) 
		VALUES(new.phone_number,new.sms_content,new.sms_type,new.create_time,new.read,
		(SELECT COUNT(sms._id) FROM sms WHERE sms.sms_type != 3 AND ((sms.sms_type=1 and sms.phone_number = new.phone_number and sms.target_phone_number = new.target_phone_number) or(sms.sms_type!=1 and sms.phone_number = new.target_phone_number and sms.target_phone_number = new.phone_number))),new.sms_resource_url,new.sms_resource_type,new.sms_resource_name,new.sms_resource_time_length,new.sms_resource_rs_ok,new.target_phone_number,new.owner_phone_number,new.is_group_message,new.uiCause); 
END;
/

#������Ϣ���в���һ�����ݵ�ʱ�򣬲��Ҳ����յ���Ϣ��ʱ����Ʃ�緢��������״̬��ʱ��
CREATE TRIGGER update_sms_conversation_on_sms_insert 
AFTER INSERT ON sms 
WHEN new.sms_type != 1
BEGIN  
	DELETE FROM sms_conversation WHERE ((sms_type=1 and phone_number = new.target_phone_number and target_phone_number = new.phone_number and owner_phone_number = new.owner_phone_number) or(sms_type!=1 and phone_number = new.phone_number and target_phone_number = new.target_phone_number and owner_phone_number = new.owner_phone_number) or (is_group_message = 1 and target_phone_number = new.target_phone_number));
	INSERT INTO sms_conversation(phone_number,sms_content,sms_type,create_time,read,sms_count,sms_resource_url,sms_resource_type,sms_resource_name,sms_resource_time_length,sms_resource_rs_ok,target_phone_number,owner_phone_number,is_group_message,uiCause) 
		VALUES(new.phone_number,new.sms_content,new.sms_type,new.create_time,1,
		(SELECT COUNT(sms._id) FROM sms WHERE sms.sms_type != 3 AND ((sms.sms_type=1 and sms.phone_number = new.target_phone_number and sms.target_phone_number = new.phone_number) or(sms.sms_type!=1 and sms.phone_number = new.phone_number and sms.target_phone_number = new.target_phone_number))),new.sms_resource_url,new.sms_resource_type,new.sms_resource_name,new.sms_resource_time_length,new.sms_resource_rs_ok,new.target_phone_number,new.owner_phone_number,new.is_group_message,new.uiCause); 
END;
/

#��ɾ��sms��һ����¼�Ժ���ôconversation����Ӧ
CREATE TRIGGER update_sms_conversation_on_sms_delete 
AFTER DELETE ON sms 
BEGIN  
	UPDATE sms_conversation SET sms_count = sms_count-1 WHERE phone_number = OLD.phone_number;
END;
/
#�����ϵĴ�����ִ�����Ժ󣬶�count�Ľ����ж�������Ϊ0����ôҪɾ�������Ự
CREATE TRIGGER sms_conversation_count_on_update 
AFTER  UPDATE OF sms_count  ON sms_conversation 
BEGIN   
	DELETE FROM sms_conversation WHERE new.sms_count = 0 AND phone_number = new.phone_number;
END;
/

