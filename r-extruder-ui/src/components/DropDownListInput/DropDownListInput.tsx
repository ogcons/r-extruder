import { DropDownList } from "@progress/kendo-react-dropdowns";
import React from "react";
import { EventDropdown, FieldRenderPropsDropdown } from "../../utils/types";
import "./DropDownListInput.scss";

const data = [
  {
    id: "getId",
    name: "Get ID",
  },
  {
    id: "getDocument",
    name: "Get document",
  },
];
const DropDownListInput = (
  fieldRenderPropsDropdown: FieldRenderPropsDropdown
) => {
  const { value, onChange, ...others } = fieldRenderPropsDropdown;

  const handleDropDownChange = (event: EventDropdown) => {
    const outputValue = event.target.value.id === "getDocument" ? "docx" : "id";
    onChange({ value: outputValue });
  };
  return (
    <DropDownList
      data={data}
      textField={"name"}
      dataItemKey={"id"}
      value={data.find((item) => item.id === value)}
      onChange={handleDropDownChange}
      {...others}
    />
  );
};

export default DropDownListInput;
