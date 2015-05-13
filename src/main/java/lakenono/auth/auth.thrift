namespace java lolth.auth.thrift

service AuthManagerService{
	map<string,string> getAuthData(1:string domain,2:string clientIp);
}