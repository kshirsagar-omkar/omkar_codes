#include<iostream>
#include<fstream>
using namespace std;


class Restaurants
{
	private:
		int id;
		string name;
		string location;
		ifstream restaurants_file;
	public:
		Restaurants(int id=0, string name = "NA", string location = "NA") : id(id), name(name),location(location) 
		{
			restaurants_file.open("restaurants_file.txt",ios::app);
		}
		Restaurants(Restaurants &tres) : id(tres.id), name(tres.name),location(tres.location) 
		{
			restaurants_file.open("restaurants_file.txt",ios::app);
		}
		
		void display_restaurants()
		{
			Restaurants temp;
			while(restaurants_file >> temp.id >> temp.name >> temp.location )
			{
				cout<<"ID		: "<< temp.id <<endl;
				cout<<"Name		: "<< temp.name <<endl;
				cout<<"Location	: "<< temp.location <<endl<<endl;
			}
		}
		
		string select_restaurants()
		{
			string id="";
			cout<<"Enter ID of The Restaurant to View Mwnu: ";
			cin>>id;
			id.insert(0,"ID : ");
			return id;
		}
		
		~Restaurants()
		{
			restaurants_file.close();
		}

};

int main()
{
	Restaurants obj;
	obj.display_restaurants();
	cout<<obj.select_restaurants()<<endl;
	
	return 0;	
}
